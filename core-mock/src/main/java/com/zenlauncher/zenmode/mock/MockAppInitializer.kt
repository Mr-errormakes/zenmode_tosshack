package com.zenlauncher.zenmode.mock

import android.app.Application
import android.util.Log
import com.zenlauncher.zenmode.coreapi.analytics.AnalyticsManager
import com.zenlauncher.zenmode.coreapi.SignInResult
import com.zenlauncher.zenmode.coreapi.User
import com.zenlauncher.zenmode.coreapi.UserStats
import com.zenlauncher.zenmode.coreapi.services.AppInitializer
import com.zenlauncher.zenmode.coreapi.services.ServiceLocator
import com.zenlauncher.zenmode.coreapi.services.AuthProvider
import com.zenlauncher.zenmode.coreapi.services.FirestoreDataSource
import com.zenlauncher.zenmode.coreapi.services.AnalyticsTrackerContract
import com.zenlauncher.zenmode.coreapi.services.RemoteConfigProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "MockAppInitializer"

/**
 * Mock implementation of [AppInitializer] used for development / open builds.
 *
 * Every field declared in [ServiceLocator] MUST be assigned here to prevent
 * [UninitializedPropertyAccessException] crashes on public startup.
 *
 * Assignment order mirrors the declaration order in ServiceLocator so it is
 * easy to spot if a new field is added but not wired up here.
 */
class MockAppInitializer : AppInitializer {

    override fun initialize(application: Application) {
        Log.i(TAG, "▶ Initializing MOCK Core Services…")

        // 1. Analytics Manager — generic event tracking
        ServiceLocator.analyticsManager = MockAnalyticsManager().also {
            Log.d(TAG, "  ✔ analyticsManager  → MockAnalyticsManager")
        }

        // 2. Analytics Tracker — structured event tracking
        ServiceLocator.analyticsTracker = MockAnalyticsTracker().also {
            Log.d(TAG, "  ✔ analyticsTracker  → MockAnalyticsTracker")
        }

        // 3. Auth Provider — user identity / sign-in
        ServiceLocator.authProvider = MockAuthProvider().also {
            Log.d(TAG, "  ✔ authProvider      → MockAuthProvider")
        }

        // 4. Firestore Data Source — backend persistence
        ServiceLocator.firestoreDataSource = MockFirestoreDataSource().also {
            Log.d(TAG, "  ✔ firestoreDataSource → MockFirestoreDataSource")
        }

        // 5. Remote Config Provider — feature flags / min version
        ServiceLocator.remoteConfigProvider = MockRemoteConfigProvider().also {
            Log.d(TAG, "  ✔ remoteConfigProvider → MockRemoteConfigProvider")
        }

        // Verify every lateinit var is initialised before the app opens any screen.
        // A false result here means a new ServiceLocator field was added without
        // a corresponding mock — fix it above before shipping.
        check(ServiceLocator.isInitialized) {
            "MockAppInitializer: ServiceLocator is NOT fully initialized! " +
            "Add missing mock stubs above so no screen crashes with " +
            "UninitializedPropertyAccessException at runtime."
        }

        Log.i(TAG, "◀ All MOCK services registered successfully. ServiceLocator.isInitialized = true")
    }
}

// ── Mock Implementations ──────────────────────────────────────────────────────

class MockRemoteConfigProvider : RemoteConfigProvider {
    /** Always returns 0 so the mock build is never force-updated. */
    override val minVersionCode: StateFlow<Long> = MutableStateFlow(0L)
    override suspend fun initialize() {
        Log.i("MockRemoteConfig", "MOCK Remote Config: defaulting minVersionCode to 0L.")
    }
}

class MockAuthProvider : AuthProvider {
    private var signedIn = false
    private val fakeUserId = "mock_user_123"

    override fun isSignedIn(): Boolean = signedIn
    override fun getCurrentUserId(): String? = if (signedIn) fakeUserId else null
    override fun getPhotoUrl(): String? = null
    override fun getEmail(): String? = null
    override fun getDisplayName(): String? = if (signedIn) "Mock User" else null

    override suspend fun signInWithGoogleToken(idToken: String): SignInResult {
        signedIn = true
        return SignInResult(
            userId = fakeUserId,
            displayName = "Mock User",
            isNewUser = false,
            isSuccess = true
        )
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInResult {
        signedIn = true
        return SignInResult(
            userId = fakeUserId,
            displayName = "Mock User",
            isNewUser = false,
            isSuccess = true,
            email = email
        )
    }

    override fun signOut() { signedIn = false }
    override suspend fun deleteAccount() { signedIn = false }
}

class MockFirestoreDataSource : FirestoreDataSource {
    override suspend fun getBuddyUid(myUid: String): String? = null
    override suspend fun getBuddyStats(buddyUid: String): UserStats? = null
    override suspend fun getUser(uid: String): User? = User(uid, "Mock User")
    override suspend fun checkRelationshipExists(myUid: String, otherUid: String): Boolean = false
    override suspend fun sendBuddyInvite(myUid: String, targetUid: String) {}
    override suspend fun disconnectBuddy(myUid: String, buddyUid: String) {}
    override suspend fun findRandomBuddy(myUid: String): String? = null
    override suspend fun initializeUser(uid: String, displayName: String?) {}
    override suspend fun deleteUser(uid: String) {}
    override suspend fun getRelationshipCreatedAt(myUid: String): Long? = null
    override fun getRelationshipId(user1: String, user2: String): String =
        if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    override suspend fun sendLike(relationshipId: String, senderUid: String): Boolean = true
    override suspend fun getTodayLikes(
        relationshipId: String,
        myUid: String,
        buddyUid: String
    ): Pair<Long, Long> = 0L to 0L
    override suspend fun saveFcmToken(uid: String, token: String) {}
}

class MockAnalyticsTracker : AnalyticsTrackerContract {
    override fun trackAppFirstOpen(source: String, device: String) {}
    override fun trackOnboardingStarted() {}
    override fun trackPermissionScreenViewed(permissionType: String) {}
    override fun trackPermissionGranted(permissionType: String) {}
    override fun trackSetupCompleted(timeTakenSec: Int, permissionsGrantedCount: Int) {}
    override fun trackDoomScrollThresholdReached(appName: String) {}
    override fun trackOverlayDismissed(type: String) {}
    override fun trackRememberMeSelected(duration: String) {}
    override fun trackOverlayActionTaken(action: String) {}
    override fun trackBuddyShareStarted(mode: String) {}
    override fun trackBuddyCodeCopied(mode: String) {}
    override fun trackBuddyCodePasted(mode: String) {}
    override fun trackBuddyConnected(mode: String) {}
}

class MockAnalyticsManager : AnalyticsManager {
    override fun trackEvent(eventName: String, properties: Map<String, Any>?) {}
    override fun identifyUser(userId: String, properties: Map<String, Any>?) {}
    override fun reset() {}
}
