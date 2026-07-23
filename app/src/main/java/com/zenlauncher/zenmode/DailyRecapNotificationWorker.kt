package com.zenlauncher.zenmode

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zenlauncher.zenmode.coreapi.UsageRepository
import com.zenlauncher.zenmode.coreapi.services.ServiceLocator

class DailyRecapNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val usageRepository = UsageRepository(applicationContext, ServiceLocator.analyticsManager)
        
        // 1. Fetch screen-time data
        val todayUsage = usageRepository.getTodayUsage()
        val todayMillis = todayUsage.screenTimeInMillis
        val todayMins = (todayMillis / 1000) / 60
        
        // 24 hours total day in minutes is 1440
        val offScreenMins = (1440 - todayMins).coerceAtLeast(0)
        val offScreenHours = offScreenMins / 60
        val remainingOffScreenMins = offScreenMins % 60

        // 2. Fetch past 30 days streak to show up-to-date milestone reward
        val past30DaysMillis = usageRepository.getPast30DaysScreenTimeMillis()
        val streakCount = AppLogic.getStreakCount(past30DaysMillis)

        // 3. Send Daily Recap notification
        sendRecapNotification(offScreenHours, remainingOffScreenMins, streakCount)

        return Result.success()
    }

    private fun sendRecapNotification(hours: Long, minutes: Long, streak: Int) {
        val channelId = "ZenDailyRecap"
        val channelName = "Daily off-screen stats recap"
        
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Summarizes off-screen minutes accomplished and updates streaks daily."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open app on tap
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Daily Recap: You're doing amazing! 🧘‍♂️"
        val body = "Streak safe: $streak days! You spent ${hours}h ${minutes}m off-screen today. Keep it up!"

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2002, notification)
    }
}
