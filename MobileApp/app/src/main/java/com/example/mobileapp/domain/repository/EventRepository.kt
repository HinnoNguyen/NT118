package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Event

interface EventRepository {
    suspend fun createEvent(
        title: String,
        description: String,
        date: Long,
        time: String,
        location: String = ""
    ): Result<Event>

    suspend fun getEvents(): Result<List<Event>>

    suspend fun getEventsByDate(date: Long): Result<List<Event>>

    suspend fun getUpcomingEvents(limit: Long = 10): Result<List<Event>>

    suspend fun updateEvent(event: Event): Result<Event>

    suspend fun deleteEvent(eventId: String): Result<Unit>
}
