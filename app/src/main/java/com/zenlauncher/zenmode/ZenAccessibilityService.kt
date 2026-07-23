package com.zenlauncher.zenmode

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class ZenAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_LOCK_SCREEN = "com.zenlauncher.zenmode.ACTION_LOCK_SCREEN"
        private var instance: ZenAccessibilityService? = null

        fun lockScreen(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ?: false
        }

        fun isRunning(): Boolean = instance != null

        fun isEnabledInSettings(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val expectedComponent = "${context.packageName}/${context.packageName}.ZenAccessibilityService"
            return enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return

        // Skip system UI, system settings, Android launchers, and our own app package
        if (packageName == applicationContext.packageName ||
            packageName == "com.android.systemui" ||
            packageName == "com.android.settings" ||
            packageName == "com.google.android.apps.nexuslauncher" ||
            packageName.contains("launcher") ||
            packageName.startsWith("com.android.")
        ) {
            return
        }

        val repository = if (com.zenlauncher.zenmode.coreapi.services.ServiceLocator.isInitialized) {
            val analyticsManager = com.zenlauncher.zenmode.coreapi.services.ServiceLocator.analyticsManager
            com.zenlauncher.zenmode.coreapi.UsageRepository(applicationContext, analyticsManager)
        } else {
            // Fallback for mock initialization if ServiceLocator wasn't populated yet
            val mockAnalytics = object : com.zenlauncher.zenmode.coreapi.analytics.AnalyticsManager {
                override fun trackEvent(eventName: String, properties: Map<String, Any>?) {}
                override fun identifyUser(userId: String, properties: Map<String, Any>?) {}
                override fun reset() {}
            }
            com.zenlauncher.zenmode.coreapi.UsageRepository(applicationContext, mockAnalytics)
        }

        // Determine if the app being opened is a distracting/doom-scroll app
        val isDistracting = DistractingAppsRepository.isDistracting(applicationContext, packageManager, packageName)

        // Two interception modes:
        // 1. Zen is LOCKED → intercept ALL non-system apps (first unlock of the day)
        // 2. Zen is UNLOCKED but app is DISTRACTING → intercept with mindful pause
        if (!repository.isZenUnlocked() || isDistracting) {
            val intent = Intent(applicationContext, DelayedUnlockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("TARGET_PACKAGE_NAME", packageName)
            }
            applicationContext.startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
