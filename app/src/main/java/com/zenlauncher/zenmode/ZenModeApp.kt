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
        for (initializer in initializers) {
            initializer.initialize(this)
        }

        // Schedule Daily off-screen recap notification worker
        scheduleDailyRecap()

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

    private fun scheduleDailyRecap() {
        val dailyRecapRequest = androidx.work.PeriodicWorkRequest.Builder(
            DailyRecapNotificationWorker::class.java,
            24,
            java.util.concurrent.TimeUnit.HOURS
        )
        .setInitialDelay(calculateInitialDelayFor9PM(), java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()

        androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyRecapWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            dailyRecapRequest
        )
    }

    private fun calculateInitialDelayFor9PM(): Long {
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(java.util.Calendar.HOUR_OF_DAY, 21) // 9 PM
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= currentTime) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - currentTime
    }
}
