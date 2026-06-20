package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.EventDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Event
import com.example.mobileapp.domain.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class EventRepositoryImpl : EventRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection("events")

    override suspend fun createEvent(
        title: String,
        description: String,
        date: Long,
        time: String,
        location: String
    ): Result<Event> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (title.isBlank()) {
            return Result.failure(Exception("Event title cannot be empty"))
        }

        return try {
            val now = System.currentTimeMillis()
            val event = Event(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title.trim(),
                description = description.trim(),
                date = date,
                time = time.trim(),
                location = location.trim(),
                createdAt = now,
                updatedAt = now
            )

            eventsCollection.document(event.id).set(event.toDto()).await()
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to create event", e))
        }
    }

    override suspend fun getEvents(): Result<List<Event>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))

        return try {
            val snapshot = eventsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val events = snapshot.documents
                .mapNotNull { it.toObject(EventDto::class.java)?.toDomain() }
                .sortedBy { it.date }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch events", e))
        }
    }

    override suspend fun getEventsByDate(date: Long): Result<List<Event>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        return try {
            val snapshot = eventsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val events = snapshot.documents
                .mapNotNull { it.toObject(EventDto::class.java)?.toDomain() }
                .filter { it.date in startOfDay until endOfDay }
                .sortedBy { it.date }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch events for date", e))
        }
    }

    override suspend fun getUpcomingEvents(limit: Long): Result<List<Event>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))

        return try {
            val now = System.currentTimeMillis()
            val snapshot = eventsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val events = snapshot.documents
                .mapNotNull { it.toObject(EventDto::class.java)?.toDomain() }
                .filter { it.date >= now }
                .sortedBy { it.date }
                .take(limit.toInt())
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch upcoming events", e))
        }
    }

    override suspend fun updateEvent(event: Event): Result<Event> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (event.userId != userId) {
            return Result.failure(Exception("Cannot update another user's event"))
        }

        return try {
            val updatedEvent = event.copy(updatedAt = System.currentTimeMillis())
            eventsCollection.document(updatedEvent.id).set(updatedEvent.toDto()).await()
            Result.success(updatedEvent)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to update event", e))
        }
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> {
        if (eventId.isBlank()) {
            return Result.failure(Exception("Event id cannot be empty"))
        }

        return try {
            eventsCollection.document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to delete event", e))
        }
    }
}
