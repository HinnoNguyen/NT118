package com.example.mobileapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mobileapp.receiver.ReminderReceiver

object ReminderManager {

    fun scheduleReminder(context: Context, userId: String, title: String, message: String, timeMs: Long) {
        if (timeMs <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("EXTRA_USER_ID", userId)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (timeMs % Int.MAX_VALUE).toInt(), // unique request code based on timestamp
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMs, pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent)
        }
    }
}
