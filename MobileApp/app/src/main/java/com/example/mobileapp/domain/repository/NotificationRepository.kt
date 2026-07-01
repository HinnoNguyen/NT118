package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun addNotification(notification: AppNotification): Result<Unit>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun deleteAllNotifications(userId: String): Result<Unit>
}
