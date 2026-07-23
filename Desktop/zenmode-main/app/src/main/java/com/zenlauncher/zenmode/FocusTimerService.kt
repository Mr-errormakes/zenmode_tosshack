package com.zenlauncher.zenmode

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the focus session countdown alive
 * even when the screen is off. Emits [remainingMs] every second via
 * a companion-object StateFlow so any Composable can collect it.
 */
class FocusTimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    companion object {
        private val _remainingMs = MutableStateFlow(0L)
        val remainingMs: StateFlow<Long> = _remainingMs.asStateFlow()

        private val _sessionActive = MutableStateFlow(false)
        val sessionActive: StateFlow<Boolean> = _sessionActive.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusTimerService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val session = FocusSessionRepository.getActiveSession(this)
        if (session == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            AppConstants.FOCUS_TIMER_NOTIFICATION_ID,
            buildNotification(session.remainingMs)
        )

        _sessionActive.value = true

        scope.launch {
            while (true) {
                val active = FocusSessionRepository.getActiveSession(this@FocusTimerService)
                if (active == null) {
                    _remainingMs.value = 0L
                    _sessionActive.value = false
                    stopSelf()
                    break
                }
                _remainingMs.value = active.remainingMs
                updateNotification(active.remainingMs)
                delay(1_000L)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        _sessionActive.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Notifications ──────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            AppConstants.FOCUS_TIMER_CHANNEL_ID,
            "ZenMode Focus Session",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows countdown for your active focus session"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun launchIntent(): PendingIntent {
        val i = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this, 0, i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val mins = remainingMs / 60_000L
        val secs = (remainingMs % 60_000L) / 1_000L
        val timeText = "%02d:%02d remaining".format(mins, secs)
        return NotificationCompat.Builder(this, AppConstants.FOCUS_TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle("🧘 Focus Session Active")
            .setContentText(timeText)
            .setContentIntent(launchIntent())
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
    }

    private fun updateNotification(remainingMs: Long) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(AppConstants.FOCUS_TIMER_NOTIFICATION_ID, buildNotification(remainingMs))
    }
}
