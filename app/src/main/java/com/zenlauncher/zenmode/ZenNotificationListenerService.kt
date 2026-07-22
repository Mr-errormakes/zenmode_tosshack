package com.zenlauncher.zenmode

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class BatchedNotification(
    val id: String,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ZenNotificationListenerService : NotificationListenerService() {

    companion object {
        /** Observable map: packageName -> active notification count. */
        val notificationCounts = mutableStateMapOf<String, Int>()

        /** Observable list of batched notifications captured during Zen Mode. */
        val batchedNotifications = mutableStateListOf<BatchedNotification>()

        /** Whether Zen Mode is currently active for notification intercepting/batching. */
        var isZenModeActive by mutableStateOf(true)

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

        fun clearBatchedNotifications(context: Context? = null) {
            batchedNotifications.clear()
            context?.let { ctx ->
                val prefs = ctx.getSharedPreferences("zen_batched_prefs", Context.MODE_PRIVATE)
                prefs.edit().remove("batched_list").apply()
            }
        }

        fun removeBatchedNotification(id: String, context: Context? = null) {
            batchedNotifications.removeAll { it.id == id }
            context?.let { ctx ->
                saveToPrefs(ctx, batchedNotifications)
            }
        }

        fun loadFromPrefs(context: Context) {
            try {
                val prefs = context.getSharedPreferences("zen_batched_prefs", Context.MODE_PRIVATE)
                val jsonStr = prefs.getString("batched_list", null) ?: return
                val array = JSONArray(jsonStr)
                val items = mutableListOf<BatchedNotification>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    items.add(
                        BatchedNotification(
                            id = obj.optString("id", ""),
                            packageName = obj.optString("packageName", ""),
                            title = obj.optString("title", ""),
                            text = obj.optString("text", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                batchedNotifications.clear()
                batchedNotifications.addAll(items)
            } catch (_: Exception) {}
        }

        private fun saveToPrefs(context: Context, list: List<BatchedNotification>) {
            try {
                val array = JSONArray()
                for (item in list) {
                    val obj = JSONObject().apply {
                        put("id", item.id)
                        put("packageName", item.packageName)
                        put("title", item.title)
                        put("text", item.text)
                        put("timestamp", item.timestamp)
                    }
                    array.put(obj)
                }
                val prefs = context.getSharedPreferences("zen_batched_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("batched_list", array.toString()).apply()
            } catch (_: Exception) {}
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        rebuildCounts()
        loadFromPrefs(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        rebuildCounts()
        if (sbn == null) return
        android.util.Log.d("ZenNotification", "Posted: ${sbn.packageName}")

        // Skip ongoing notifications (music players, VPNs, etc.) and our own package
        if (sbn.isOngoing || sbn.packageName == packageName) return

        if (isZenModeActive) {
            interceptAndBatch(sbn)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        rebuildCounts()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        notificationCounts.clear()
    }

    private fun interceptAndBatch(sbn: StatusBarNotification) {

        val extras = sbn.notification?.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val pkg = sbn.packageName
        val id = sbn.key ?: "${pkg}_${sbn.id}_${sbn.postTime}"

        val item = BatchedNotification(
            id = id,
            packageName = pkg,
            title = title.ifBlank { pkg },
            text = text,
            timestamp = sbn.postTime
        )

        // Cancel notification to mute/intercept OS alert
        try {
            cancelNotification(sbn.key)
        } catch (_: Exception) {}

        if (batchedNotifications.none { it.id == item.id }) {
            batchedNotifications.add(0, item)
            saveToPrefs(this, batchedNotifications)
        }
    }

    private fun rebuildCounts() {
        val active = try {
            activeNotifications
        } catch (_: Exception) {
            return
        }
        val counts = mutableMapOf<String, Int>()
        for (sbn in active) {
            if (!sbn.isOngoing && sbn.packageName != packageName) {
                val pkg = sbn.packageName
                counts[pkg] = (counts[pkg] ?: 0) + 1
            }
        }
        notificationCounts.clear()
        notificationCounts.putAll(counts)
    }
}


