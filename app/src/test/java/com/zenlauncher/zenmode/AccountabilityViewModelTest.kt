package com.zenlauncher.zenmode

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.zenlauncher.zenmode.coreapi.UsageRepository
import com.zenlauncher.zenmode.coreapi.services.AuthProvider
import com.zenlauncher.zenmode.coreapi.services.FirestoreDataSource
import com.zenlauncher.zenmode.coreapi.services.ServiceLocator
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class AccountabilityViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: UsageRepository
    private lateinit var authProvider: AuthProvider
    private lateinit var firestoreDataSource: FirestoreDataSource

    @Before
    fun setUp() {
        repository = mock(UsageRepository::class.java)
        authProvider = mock(AuthProvider::class.java)
        firestoreDataSource = mock(FirestoreDataSource::class.java)

        ServiceLocator.authProvider = authProvider
        ServiceLocator.firestoreDataSource = firestoreDataSource
        // buddyReactedEvents is a val property with a default MutableSharedFlow
    }

    @Test
    fun `sendLike posts rate limit toast when max count reached`() {
        // Arrange
        whenever(repository.getUserUid()).thenReturn("user1")
        whenever(repository.getBuddyUid()).thenReturn("buddy1")
        
        // Mock that we hit the limit (4 timestamps)
        val now = System.currentTimeMillis()
        val oldest = now - (5 * 60_000L) // 5 mins ago
        whenever(repository.getRecentLikeTimestamps()).thenReturn(
            listOf(oldest, oldest + 1000, oldest + 2000, oldest + 3000)
        )

        val viewModel = AccountabilityViewModel(repository)

        // Act
        viewModel.sendLike()

        // Assert
        val toast = viewModel.likeToast.value
        assert(toast != null && toast.contains("Wait ")) { "Toast should inform user to wait: $toast" }
    }
}
