package com.zenlauncher.zenmode

import android.content.Context
import android.content.SharedPreferences
import com.zenlauncher.zenmode.coreapi.UsageRepository
import com.zenlauncher.zenmode.coreapi.analytics.AnalyticsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [UsageRepository].
 *
 * Member 4 focus: the buddy reaction rate-limiter (max 4 likes per 20-minute window).
 * The rate-limit logic lives entirely in three methods:
 *   - [UsageRepository.getRecentLikeTimestamps]  — fetch & auto-prune expired entries
 *   - [UsageRepository.recordLikeSent]            — persist a new like timestamp
 *   - [UsageRepository.removeLastLikeTimestamp]   — roll back if the network call fails
 *
 * SharedPreferences is mocked so no Android runtime is required.
 */
class UsageRepositoryTest {

    // ── Test helpers ──────────────────────────────────────────────────────────

    private fun createMockedRepository(): Triple<UsageRepository, SharedPreferences, SharedPreferences.Editor> {
        val context = mock<Context>()
        val prefs   = mock<SharedPreferences>()
        val editor  = mock<SharedPreferences.Editor>()

        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putInt(any(), any())).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(editor.putLong(any(), any())).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editor)
        whenever(editor.remove(any())).thenReturn(editor)

        val analyticsManager = mock<AnalyticsManager>()
        val repository = UsageRepository(context, analyticsManager)
        return Triple(repository, prefs, editor)
    }

    /** Convenience: return a comma-delimited timestamp string for [prefs]. */
    private fun fakeTimestampPrefs(prefs: SharedPreferences, vararg offsets: Long) {
        val now = System.currentTimeMillis()
        val raw = offsets.joinToString(",") { offset -> (now + offset).toString() }
        whenever(prefs.getString(eq("recent_like_timestamps"), any())).thenReturn(raw)
    }

    // ── Existing general tests ────────────────────────────────────────────────

    @Test
    fun `updateScreenTime increases total time`() {
        val (repository, prefs, editor) = createMockedRepository()

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        whenever(prefs.getString(eq("last_date_screentime"), any())).thenReturn(today)
        whenever(prefs.getLong("daily_screen_time", 0)).thenReturn(1000L)

        repository.updateScreenTime(5000L)

        verify(editor).putLong("daily_screen_time", 6000L)
        verify(editor).apply()
    }

    @Test
    fun `onboarding status is retrieved correctly`() {
        val (repository, prefs, _) = createMockedRepository()
        whenever(prefs.getBoolean("is_onboarding_complete", false)).thenReturn(true)
        assertEquals(true, repository.isOnboardingComplete())
    }

    @Test
    fun `setOnboardingComplete persists value`() {
        val (repository, _, editor) = createMockedRepository()
        repository.setOnboardingComplete(true)
        verify(editor).putBoolean("is_onboarding_complete", true)
        verify(editor).commit()
    }

    @Test
    fun `onboarding current page is stored and retrieved`() {
        val (repository, prefs, editor) = createMockedRepository()
        repository.setOnboardingCurrentPage(3)
        verify(editor).putInt("onboarding_current_page", 3)
        verify(editor).apply()

        whenever(prefs.getInt("onboarding_current_page", 0)).thenReturn(3)
        assertEquals(3, repository.getOnboardingCurrentPage())
    }

    @Test
    fun `clearOnboardingCurrentPage removes key`() {
        val (repository, _, editor) = createMockedRepository()
        repository.clearOnboardingCurrentPage()
        verify(editor).remove("onboarding_current_page")
        verify(editor).apply()
    }

    @Test
    fun `zen unlock flag operations work correctly`() {
        val (repository, prefs, editor) = createMockedRepository()
        repository.setZenUnlockFlag(true)
        verify(editor).putBoolean("is_zen_unlocked", true)
        repository.resetZenUnlockFlag()
        verify(editor).putBoolean("is_zen_unlocked", false)
        whenever(prefs.getBoolean(eq("is_zen_unlocked"), any())).thenReturn(true)
        assertEquals(true, repository.isZenUnlocked())
    }

    @Test
    fun `first run flag operations work correctly`() {
        val (repository, prefs, editor) = createMockedRepository()
        whenever(prefs.getBoolean("is_first_run", true)).thenReturn(true)
        assertEquals(true, repository.isFirstRun())
        repository.setFirstRunComplete()
        verify(editor).putBoolean("is_first_run", false)
        verify(editor).apply()
    }

    @Test
    fun `user uid can be saved and retrieved`() {
        val (repository, prefs, editor) = createMockedRepository()
        repository.saveUserUid("test-uid-123")
        verify(editor).putString("user_uid", "test-uid-123")
        whenever(prefs.getString("user_uid", null)).thenReturn("test-uid-123")
        assertEquals("test-uid-123", repository.getUserUid())
    }

    @Test
    fun `buddy uid can be saved and retrieved`() {
        val (repository, prefs, editor) = createMockedRepository()
        repository.saveBuddyUid("buddy-uid-456")
        verify(editor).putString("buddy_uid", "buddy-uid-456")
        whenever(prefs.getString("buddy_uid", null)).thenReturn("buddy-uid-456")
        assertEquals("buddy-uid-456", repository.getBuddyUid())
    }

    @Test
    fun `hasCachedBuddy checks prefs contains key`() {
        val (repository, prefs, _) = createMockedRepository()
        whenever(prefs.contains("buddy_uid")).thenReturn(true)
        assertEquals(true, repository.hasCachedBuddy())
        whenever(prefs.contains("buddy_uid")).thenReturn(false)
        assertEquals(false, repository.hasCachedBuddy())
    }

    @Test
    fun `buddy screen time can be saved and retrieved`() {
        val (repository, prefs, editor) = createMockedRepository()
        repository.saveBuddyScreenTime(120L)
        verify(editor).putLong("buddy_screen_time", 120L)
        whenever(prefs.getLong("buddy_screen_time", 0)).thenReturn(120L)
        assertEquals(120L, repository.getBuddyScreenTime())
    }

    @Test
    fun `clearCachedBuddy removes buddy keys`() {
        val (repository, _, editor) = createMockedRepository()
        repository.clearCachedBuddy()
        verify(editor).remove("buddy_uid")
        verify(editor).remove("buddy_screen_time")
        verify(editor).apply()
    }

    @Test
    fun `last stats processed time can be saved and retrieved`() {
        val (repository, prefs, editor) = createMockedRepository()
        repository.updateLastStatsProcessedTime(1234567890L)
        verify(editor).putLong("last_stats_processed_timestamp", 1234567890L)
        whenever(prefs.getLong("last_stats_processed_timestamp", 0L)).thenReturn(1234567890L)
        assertEquals(1234567890L, repository.getLastStatsProcessedTime())
    }

    // ── Member 4: Buddy Reaction Rate-Limiter Tests ───────────────────────────
    //
    // Rate-limit contract:
    //   • Window  = LIKE_WINDOW_MS   (20 minutes  = 1_200_000 ms)
    //   • Maximum = LIKE_MAX_COUNT   (4 likes per window)
    //
    // Persistence key: "recent_like_timestamps" — comma-delimited epoch longs.
    // Only timestamps within the last 20 minutes are considered "recent".

    @Test
    fun `getRecentLikeTimestamps returns empty list when prefs is empty`() {
        val (repository, prefs, _) = createMockedRepository()
        whenever(prefs.getString(eq("recent_like_timestamps"), any())).thenReturn("")

        val result = repository.getRecentLikeTimestamps()

        assertTrue("Expected empty list for empty prefs", result.isEmpty())
    }

    @Test
    fun `getRecentLikeTimestamps filters out timestamps older than 20 minutes`() {
        val (repository, prefs, _) = createMockedRepository()

        // expired: 25 minutes ago — must be pruned
        // valid  :  5 minutes ago — must be kept
        fakeTimestampPrefs(prefs,
            -(25 * 60_000L),   // expired
            -(5  * 60_000L)    // valid
        )

        val result = repository.getRecentLikeTimestamps()

        assertEquals("Only 1 timestamp should survive pruning", 1, result.size)
    }

    @Test
    fun `getRecentLikeTimestamps keeps all timestamps inside the 20-minute window`() {
        val (repository, prefs, _) = createMockedRepository()

        // 4 likes spread across the last 19 minutes — all valid
        fakeTimestampPrefs(prefs,
            -(19 * 60_000L),
            -(14 * 60_000L),
            -(9  * 60_000L),
            -(2  * 60_000L)
        )

        val result = repository.getRecentLikeTimestamps()

        assertEquals("All 4 timestamps within window must be returned", 4, result.size)
    }

    @Test
    fun `getRecentLikeTimestamps returns results in ascending order`() {
        val (repository, prefs, _) = createMockedRepository()

        fakeTimestampPrefs(prefs,
            -(9  * 60_000L),   // older
            -(2  * 60_000L)    // newer — stored second but should sort correctly
        )

        val result = repository.getRecentLikeTimestamps()

        assertTrue("List should be sorted oldest-first", result.first() < result.last())
    }

    @Test
    fun `getRecentLikeTimestamps discards exactly-expired boundary timestamp`() {
        val (repository, prefs, _) = createMockedRepository()

        // Exactly at the boundary (20 min ago) — NOT inside the window
        val window = UsageRepository.LIKE_WINDOW_MS
        val now    = System.currentTimeMillis()
        val exactBoundary = now - window          // equals 20 min, not < 20 min
        val justInside    = now - (window - 1_000) // 19m 59s — must be kept

        whenever(prefs.getString(eq("recent_like_timestamps"), any()))
            .thenReturn("$exactBoundary,$justInside")

        val result = repository.getRecentLikeTimestamps()

        assertEquals("Boundary-expired entry must be pruned, only 1 valid", 1, result.size)
    }

    @Test
    fun `recordLikeSent appends a new timestamp and drops all expired ones`() {
        val (repository, prefs, editor) = createMockedRepository()

        val now     = System.currentTimeMillis()
        val expired = now - (25 * 60_000L)
        val valid   = now - (5  * 60_000L)
        whenever(prefs.getString(eq("recent_like_timestamps"), any()))
            .thenReturn("$expired,$valid")

        repository.recordLikeSent()

        // After recordLikeSent, the saved string must:
        //   • NOT contain the expired timestamp
        //   • contain the valid timestamp
        //   • contain exactly 2 entries (valid + newly added)
        verify(editor).putString(
            eq("recent_like_timestamps"),
            argThat { str ->
                !str.contains(expired.toString()) &&
                str.contains(valid.toString()) &&
                str.split(",").size == 2
            }
        )
        verify(editor).apply()
    }

    @Test
    fun `recordLikeSent with all expired timestamps results in exactly 1 entry`() {
        val (repository, prefs, editor) = createMockedRepository()

        val now = System.currentTimeMillis()
        val old1 = now - (30 * 60_000L)
        val old2 = now - (25 * 60_000L)
        whenever(prefs.getString(eq("recent_like_timestamps"), any()))
            .thenReturn("$old1,$old2")

        repository.recordLikeSent()

        verify(editor).putString(
            eq("recent_like_timestamps"),
            argThat { str -> str.split(",").size == 1 }
        )
    }

    @Test
    fun `recordLikeSent with 3 valid timestamps results in exactly 4 entries`() {
        val (repository, prefs, editor) = createMockedRepository()

        val now = System.currentTimeMillis()
        val t1  = now - (15 * 60_000L)
        val t2  = now - (10 * 60_000L)
        val t3  = now - (5  * 60_000L)
        whenever(prefs.getString(eq("recent_like_timestamps"), any()))
            .thenReturn("$t1,$t2,$t3")

        repository.recordLikeSent()

        verify(editor).putString(
            eq("recent_like_timestamps"),
            argThat { str -> str.split(",").size == 4 }
        )
    }

    @Test
    fun `removeLastLikeTimestamp removes the most recent (last) timestamp`() {
        val (repository, prefs, editor) = createMockedRepository()

        val now = System.currentTimeMillis()
        val t1  = now - 10_000L  // older
        val t2  = now - 2_000L   // newer — this one should be removed

        whenever(prefs.getString(eq("recent_like_timestamps"), any()))
            .thenReturn("$t1,$t2")

        repository.removeLastLikeTimestamp()

        // Only t1 (older) remains after rollback
        verify(editor).putString(eq("recent_like_timestamps"), eq(t1.toString()))
        verify(editor).apply()
    }

    @Test
    fun `removeLastLikeTimestamp with single entry results in empty string`() {
        val (repository, prefs, editor) = createMockedRepository()

        val now = System.currentTimeMillis()
        val t1  = now - 2_000L

        whenever(prefs.getString(eq("recent_like_timestamps"), any()))
            .thenReturn("$t1")

        repository.removeLastLikeTimestamp()

        verify(editor).putString(eq("recent_like_timestamps"), eq(""))
        verify(editor).apply()
    }

    @Test
    fun `removeLastLikeTimestamp on empty prefs is a no-op`() {
        val (repository, prefs, editor) = createMockedRepository()

        whenever(prefs.getString(eq("recent_like_timestamps"), any())).thenReturn("")

        repository.removeLastLikeTimestamp()

        // Nothing should be written when there is nothing to remove
        verify(editor, never()).putString(any(), any())
    }

    @Test
    fun `rate limit constant LIKE_MAX_COUNT equals 4`() {
        assertEquals(
            "Rate limit must be exactly 4 likes per window",
            4, UsageRepository.LIKE_MAX_COUNT
        )
    }

    @Test
    fun `rate limit constant LIKE_WINDOW_MS equals 20 minutes`() {
        val twentyMinutes = 20L * 60_000L
        assertEquals(
            "Window must be exactly 20 minutes in milliseconds",
            twentyMinutes, UsageRepository.LIKE_WINDOW_MS
        )
    }
}
