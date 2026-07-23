package com.zenlauncher.zenmode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zenlauncher.zenmode.coreapi.DailyUsage
import com.zenlauncher.zenmode.coreapi.UsageRepository
import com.zenlauncher.zenmode.coreapi.services.ServiceLocator
import kotlinx.coroutines.launch

data class AccountabilityUiState(
    val myUsage: DailyUsage = DailyUsage(0L),
    val buddyStats: BuddyStats? = null,
    val userCode: String? = null,
    val buddyUid: String? = null,
    val connectionDateMillis: Long? = null,
    val disconnectResult: DisconnectResult? = null
)

sealed class DisconnectResult {
    data object Success : DisconnectResult()
    data class Error(val message: String) : DisconnectResult()
}

/**
 * ViewModel for the Buddy Accountability feature.
 *
 * ## Buddy Reaction Rate-Limiter
 * Users may send at most [UsageRepository.LIKE_MAX_COUNT] (4) reactions within
 * any rolling [UsageRepository.LIKE_WINDOW_MS] (20-minute) window.
 *
 * The contract is enforced entirely in [sendLike]:
 *  1. [UsageRepository.getRecentLikeTimestamps] returns only timestamps that fall
 *     inside the current 20-minute window (older ones are auto-pruned on read).
 *  2. If `size >= LIKE_MAX_COUNT` the send is blocked and a wait-time toast is shown.
 *  3. On success: [UsageRepository.recordLikeSent] persists the new timestamp.
 *  4. On network failure: [UsageRepository.removeLastLikeTimestamp] rolls back.
 *
 * The [remainingLikes] LiveData is derived from the timestamp list and emitted every
 * time [sendLike] is called so the UI can reflect the remaining quota in real-time
 * using the Reddit Mono typeface for numerical display.
 */
class AccountabilityViewModel(private val repository: UsageRepository) : ViewModel() {

    private val firestoreDataSource = ServiceLocator.firestoreDataSource

    private val _uiState = MutableLiveData(AccountabilityUiState())
    val uiState: LiveData<AccountabilityUiState> get() = _uiState

    private val _myLikes = MutableLiveData<Long>(0L)
    val myLikes: LiveData<Long> get() = _myLikes

    private val _buddyLikes = MutableLiveData<Long>(0L)
    val buddyLikes: LiveData<Long> get() = _buddyLikes

    /**
     * Human-readable toast to show when the like is blocked by the rate-limiter,
     * or null when idle.  Format: "Wait Xm Ys to react again".
     *
     * Bind this to a Snackbar / Toast in the UI.  Call [clearLikeToast] after it
     * has been displayed so it doesn't re-surface on config change.
     */
    private val _likeToast = MutableLiveData<String?>()
    val likeToast: LiveData<String?> get() = _likeToast

    /**
     * Number of reactions the user can still send in the current 20-minute window.
     * Range: 0 – [UsageRepository.LIKE_MAX_COUNT] (i.e. 0 – 4).
     *
     * **Display with Reddit Mono typeface** so the counter digits have consistent
     * width and don't cause layout shift as the number changes.
     */
    private val _remainingLikes = MutableLiveData<Int>(UsageRepository.LIKE_MAX_COUNT)
    val remainingLikes: LiveData<Int> get() = _remainingLikes

    init {
        loadData()
        refreshRemainingLikes()

        // Live-refresh likes when a buddy_react FCM push arrives while the app is foregrounded.
        viewModelScope.launch {
            ServiceLocator.buddyReactedEvents.collect {
                val myUid    = repository.getUserUid()
                    ?: ServiceLocator.authProvider.getCurrentUserId()
                    ?: return@collect
                val buddyUid = repository.getBuddyUid() ?: return@collect
                val relId    = firestoreDataSource.getRelationshipId(myUid, buddyUid)
                val (mine, buddy) = firestoreDataSource.getTodayLikes(relId, myUid, buddyUid)
                _myLikes.postValue(mine)
                _buddyLikes.postValue(buddy)
            }
        }
    }

    // ── Data loading ─────────────────────────────────────────────────────────

    private fun loadData() {
        val weeklyTotalMillis = repository.getWeeklyScreenTimeMillis().sum()
        val myUsage           = DailyUsage(weeklyTotalMillis)
        val buddyStats        = if (repository.hasCachedBuddy()) BuddyStats(repository.getBuddyScreenTime()) else null
        val userCode          = repository.getUserUid() ?: ServiceLocator.authProvider.getCurrentUserId()
        val buddyUid          = repository.getBuddyUid()

        _uiState.value = AccountabilityUiState(
            myUsage            = myUsage,
            buddyStats         = buddyStats,
            userCode           = userCode,
            buddyUid           = buddyUid,
            connectionDateMillis = repository.getBuddyConnectionDate()
        )

        // Fetch connection date from Firestore if not yet cached locally.
        if (repository.getBuddyConnectionDate() == null && userCode != null) {
            viewModelScope.launch {
                val fetched = firestoreDataSource.getRelationshipCreatedAt(userCode)
                if (fetched != null) {
                    repository.saveBuddyConnectionDate(fetched)
                    _uiState.postValue(_uiState.value!!.copy(connectionDateMillis = fetched))
                }
            }
        }

        if (userCode != null && buddyUid != null) {
            viewModelScope.launch {
                val relId         = firestoreDataSource.getRelationshipId(userCode, buddyUid)
                val (mine, buddy) = firestoreDataSource.getTodayLikes(relId, userCode, buddyUid)
                _myLikes.postValue(mine)
                _buddyLikes.postValue(buddy)
            }
        }
    }

    // ── Rate-limiter helpers ──────────────────────────────────────────────────

    /**
     * Returns true if the user has at least one remaining like in the current window.
     * Callers should use this before showing the heart button as enabled.
     */
    fun canSendLike(): Boolean =
        repository.getRecentLikeTimestamps().size < UsageRepository.LIKE_MAX_COUNT

    /**
     * Refreshes [remainingLikes] from the repository.
     * Call this when the screen resumes so the counter is always up-to-date
     * (timestamps may have expired while the screen was backgrounded).
     */
    fun refreshRemainingLikes() {
        val used      = repository.getRecentLikeTimestamps().size
        val remaining = (UsageRepository.LIKE_MAX_COUNT - used).coerceAtLeast(0)
        _remainingLikes.value = remaining
    }

    // ── Like action ───────────────────────────────────────────────────────────

    /**
     * Attempts to send a buddy reaction.
     *
     * Rate-limit enforcement:
     *   - If the 20-minute quota (4 likes) is exhausted, emit a wait-time toast
     *     and return without touching the network.
     *   - On optimistic UI update, immediately reflect the new count locally.
     *   - On network failure, roll back the local count AND the persisted timestamp.
     */
    fun sendLike() {
        val myUid    = repository.getUserUid()
            ?: ServiceLocator.authProvider.getCurrentUserId()
            ?: return
        val buddyUid = repository.getBuddyUid() ?: return

        // ── Rate-limit check ──────────────────────────────────────────────────
        val recent = repository.getRecentLikeTimestamps()
        if (recent.size >= UsageRepository.LIKE_MAX_COUNT) {
            val oldestTimestamp = recent.first()
            val windowExpiry    = oldestTimestamp + UsageRepository.LIKE_WINDOW_MS
            val waitMs          = windowExpiry - System.currentTimeMillis()
            _likeToast.postValue(formatWaitToast(waitMs))
            return
        }

        // ── Optimistic update ─────────────────────────────────────────────────
        val previous = _myLikes.value ?: 0L
        _myLikes.postValue(previous + 1)
        repository.recordLikeSent()
        refreshRemainingLikes()   // update the quota counter immediately

        // ── Network call with rollback on failure ─────────────────────────────
        viewModelScope.launch {
            val relId = firestoreDataSource.getRelationshipId(myUid, buddyUid)
            val ok    = firestoreDataSource.sendLike(relId, myUid)
            if (!ok) {
                // Roll back both the displayed count and the persisted timestamp.
                _myLikes.postValue(previous)
                repository.removeLastLikeTimestamp()
                refreshRemainingLikes()
                _likeToast.postValue("Failed to react — please try again")
            }
        }
    }

    fun clearLikeToast() { _likeToast.value = null }

    fun reload() { loadData(); refreshRemainingLikes() }

    fun resetDisconnectResult() {
        _uiState.postValue(_uiState.value!!.copy(disconnectResult = null))
    }

    fun disconnectBuddy() {
        val myUid    = repository.getUserUid()
            ?: ServiceLocator.authProvider.getCurrentUserId()
            ?: return
        val buddyUid = repository.getBuddyUid() ?: return

        viewModelScope.launch {
            try {
                firestoreDataSource.disconnectBuddy(myUid, buddyUid)
                repository.clearCachedBuddy()
                _uiState.postValue(_uiState.value!!.copy(disconnectResult = DisconnectResult.Success))
            } catch (e: Exception) {
                _uiState.postValue(
                    _uiState.value!!.copy(
                        disconnectResult = DisconnectResult.Error(e.message ?: "Unknown error")
                    )
                )
            }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Formats [waitMs] into a human-readable string for the rate-limit toast.
     *
     * Examples:
     *  -  62_000 ms  →  "Wait 1m 2s to react again"
     *  -   8_000 ms  →  "Wait 8s to react again"
     */
    private fun formatWaitToast(waitMs: Long): String {
        val safe     = waitMs.coerceAtLeast(0L)
        val totalSec = (safe + 999L) / 1000L
        val mins     = totalSec / 60L
        val secs     = totalSec % 60L
        return if (mins > 0) "Wait ${mins}m ${secs}s to react again"
               else          "Wait ${secs}s to react again"
    }
}

class AccountabilityViewModelFactory(
    private val repository: UsageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountabilityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountabilityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
