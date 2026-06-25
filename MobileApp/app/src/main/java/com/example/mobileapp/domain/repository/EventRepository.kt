package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Event
import com.example.mobileapp.utils.Resource

interface EventRepository {
    suspend fun getEventsForDate(userId: String, dayStart: Long, dayEnd: Long): Resource<List<Event>>
    suspend fun addEvent(userId: String, title: String, time: String, description: String, date: Long): Resource<Event>
    suspend fun deleteEvent(userId: String, eventId: String): Resource<Unit>
}
