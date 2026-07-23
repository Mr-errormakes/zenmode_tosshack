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

        fun isMindlessScrollingActive(): Boolean {
            return instance?.isMindless ?: false
        }

        fun isEnabledInSettings(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val expectedComponent = "${context.packageName}/${context.packageName}.ZenAccessibilityService"
            return enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
        }
    }

    private var isMindless = false
    private val lastScrollTimes = ArrayList<Long>()
    private var lastInteractionTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val now = System.currentTimeMillis()
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                lastScrollTimes.removeAll { now - it > 60000L }
                lastScrollTimes.add(now)
                evaluateMindlessState(now)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                lastInteractionTime = now
                isMindless = false
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                lastScrollTimes.clear()
                lastInteractionTime = now
                isMindless = false
            }
        }
    }

    private fun evaluateMindlessState(now: Long) {
        val timeSinceLastInteraction = now - lastInteractionTime
        isMindless = (lastScrollTimes.size >= 6) && (timeSinceLastInteraction > 12000L)
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
