package com.example.mobileapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mobileapp.data.repository.NotificationRepositoryImpl
import com.example.mobileapp.domain.model.AppNotification
import com.example.mobileapp.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val userId = intent.getStringExtra("EXTRA_USER_ID") ?: ""
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Reminder"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "You have a scheduled reminder"
        
        // 1. Show System Notification
        NotificationHelper.showReminder(context, title, message)

        // 2. Add to In-App Notifications
        if (userId.isNotEmpty()) {
            val repository = NotificationRepositoryImpl()
            val appNotif = AppNotification(
                userId = userId,
                title = title,
                message = message,
                type = "calendar", // Using calendar type for reminders
                timestamp = System.currentTimeMillis()
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                repository.addNotification(appNotif)
            }
        }
    }
}
