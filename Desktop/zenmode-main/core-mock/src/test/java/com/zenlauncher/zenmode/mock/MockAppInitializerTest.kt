package com.zenlauncher.zenmode.mock

import android.app.Application
import com.zenlauncher.zenmode.coreapi.services.ServiceLocator
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class MockAppInitializerTest {

    @Test
    fun `initialize correctly populates ServiceLocator without UninitializedPropertyAccessException`() {
        val mockApp = object : Application() {}
        val initializer = MockAppInitializer()

        // Executing the initializer
        initializer.initialize(mockApp)

        // Verifying that all lateinit vars on ServiceLocator are initialized
        // This will throw UninitializedPropertyAccessException if any is missing
        val isFullInit = ServiceLocator.isInitialized
        assertTrue("ServiceLocator should be fully initialized", isFullInit)
    }
}
