package com.example.mobileapp.data.repository

import com.example.mobileapp.domain.model.AppNotification
import com.example.mobileapp.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : NotificationRepository {

    private val notificationsCollection = firestore.collection("notifications")

    override fun getNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val registration = notificationsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.toObjects(AppNotification::class.java)
                        .sortedByDescending { it.timestamp }
                    trySend(notifications)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addNotification(notification: AppNotification): Result<Unit> {
        return try {
            val docRef = notificationsCollection.document()
            val notificationWithId = notification.copy(id = docRef.id)
            docRef.set(notificationWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAllNotifications(userId: String): Result<Unit> {
        return try {
            val snapshot = notificationsCollection.whereEqualTo("userId", userId).get().await()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
