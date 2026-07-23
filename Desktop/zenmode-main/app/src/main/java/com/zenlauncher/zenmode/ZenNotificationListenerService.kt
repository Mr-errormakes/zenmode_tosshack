package com.zenlauncher.zenmode

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.mutableStateMapOf

class ZenNotificationListenerService : NotificationListenerService() {

    companion object {
        /** Observable map: packageName -> active notification count. */
        val notificationCounts = mutableStateMapOf<String, Int>()

        private var instance: ZenNotificationListenerService? = null

        fun isRunning(): Boolean = instance != null

        fun isEnabledInSettings(context: Context): Boolean {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val componentName = ComponentName(context, ZenNotificationListenerService::class.java)
            return flat.contains(componentName.flattenToString())
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        rebuildCounts()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let { notification ->
            // If focus session is active, cancel notifications from distracting apps
            val isFocusActive = FocusSessionRepository.isSessionActive(applicationContext)
            if (isFocusActive && !notification.isOngoing && notification.packageName != packageName) {
                if (DistractingAppsRepository.isDistracting(applicationContext, packageManager, notification.packageName)) {
                    cancelNotification(notification.key)
                }
            }
        }
        rebuildCounts()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        rebuildCounts()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        notificationCounts.clear()
    }

    private fun rebuildCounts() {
        val active = try {
            activeNotifications
        } catch (_: Exception) {
            return
        }
        val isFocusActive = FocusSessionRepository.isSessionActive(applicationContext)
        val counts = mutableMapOf<String, Int>()
        for (sbn in active) {
            // Skip ongoing notifications (music players, VPNs, etc.)
            // and skip our own notifications
            if (sbn.isOngoing || sbn.packageName == packageName) continue
            // During a focus session, mute badges from distracting apps
            if (isFocusActive && DistractingAppsRepository.isDistracting(
                    applicationContext, packageManager, sbn.packageName)) continue
            val pkg = sbn.packageName
            counts[pkg] = (counts[pkg] ?: 0) + 1
        }
        notificationCounts.clear()
        notificationCounts.putAll(counts)
    }
}
