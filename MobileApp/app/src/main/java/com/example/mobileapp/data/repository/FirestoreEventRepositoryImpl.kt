package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.EventDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.domain.model.Event
import com.example.mobileapp.domain.repository.EventRepository
import com.example.mobileapp.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreEventRepositoryImpl : EventRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun eventsCollection(userId: String) =
        db.collection("users").document(userId).collection("events")

    override suspend fun getEventsForDate(userId: String, dayStart: Long, dayEnd: Long): Resource<List<Event>> = try {
        val snapshot = eventsCollection(userId)
            .whereGreaterThanOrEqualTo("date", dayStart)
            .whereLessThanOrEqualTo("date", dayEnd)
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("time", Query.Direction.ASCENDING)
            .get().await()
        Resource.Success(snapshot.documents.mapNotNull { it.toObject(EventDto::class.java)?.toDomain() })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load events")
    }

    override suspend fun addEvent(
        userId: String,
        title: String,
        time: String,
        description: String,
        date: Long
    ): Resource<Event> = try {
        val docRef = eventsCollection(userId).document()
        val now = System.currentTimeMillis()
        val dto = EventDto(
            id = docRef.id,
            userId = userId,
            title = title,
            date = date,
            time = time,
            description = description,
            createdAt = now
        )
        docRef.set(dto).await()
        Resource.Success(dto.toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to save event")
    }

    override suspend fun deleteEvent(userId: String, eventId: String): Resource<Unit> = try {
        eventsCollection(userId).document(eventId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to delete event")
    }
}
