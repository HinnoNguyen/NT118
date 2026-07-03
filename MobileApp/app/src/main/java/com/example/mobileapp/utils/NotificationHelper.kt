package com.example.mobileapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_ID = "timer_channel"
    const val REMINDER_CHANNEL_ID = "reminder_channel"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val timerChannel = NotificationChannel(
                CHANNEL_ID, "Timer Notifications", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Notifies when a Pomodoro work or break session ends" }
            
            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID, "Notes Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifies for scheduled note reminders" }

            manager?.createNotificationChannel(timerChannel)
            manager?.createNotificationChannel(reminderChannel)
        }
    }

    fun showReminder(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
        }
    }

    fun showSessionComplete(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted — silently skip, the in-app state still updates
        }
    }
}
