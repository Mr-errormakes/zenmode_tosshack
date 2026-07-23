package com.zenlauncher.zenmode

import android.app.Application
import com.zenlauncher.zenmode.coreapi.UsageRepository
import com.zenlauncher.zenmode.coreapi.services.AppInitializer
import com.zenlauncher.zenmode.coreapi.services.ServiceLocator
import java.util.ServiceLoader

class ZenModeApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Restore persisted dark/light theme before any activity renders
        ThemePreferences.applyStoredTheme(this)

        // Discover and run the private module initializer via ServiceLoader
        val initializers = ServiceLoader.load(AppInitializer::class.java)
        var initialized = false
        for (initializer in initializers) {
            initializer.initialize(this)
            initialized = true
        }

        // Fallback to MockAppInitializer if ServiceLoader didn't initialize ServiceLocator
        if (!initialized || !ServiceLocator.isInitialized) {
            try {
                val mockInitializerClass = Class.forName("com.zenlauncher.zenmode.mock.MockAppInitializer")
                val initializer = mockInitializerClass.getDeclaredConstructor().newInstance() as AppInitializer
                initializer.initialize(this)
            } catch (e: Exception) {
                // Mock initializer not found or failed to load
            }
        }

        // Track App First Open (only if ServiceLocator was populated)
        if (ServiceLocator.isInitialized) {
            val analyticsTracker = ServiceLocator.analyticsTracker
            val analyticsManager = ServiceLocator.analyticsManager
            val repository = UsageRepository(this, analyticsManager)

            if (repository.isFirstRun()) {
                analyticsTracker.trackAppFirstOpen(
                    source = "direct", // Placeholder source
                    device = android.os.Build.MODEL
                )
                repository.setFirstRunComplete()
            }

            // Re-identify existing signed-in users (one-time backfill)
            if (ServiceLocator.authProvider.isSignedIn() && !repository.isPostHogIdentified()) {
                val authProvider = ServiceLocator.authProvider
                val userId = authProvider.getCurrentUserId()
                if (userId != null) {
                    analyticsManager.identifyUser(userId, mapOf(
                        "name" to (authProvider.getDisplayName() ?: ""),
                        "email" to (authProvider.getEmail() ?: "")
                    ))
                    repository.setPostHogIdentified(true)
                }
            }
        }
    }
}
