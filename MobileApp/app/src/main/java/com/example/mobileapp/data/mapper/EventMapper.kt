package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.EventDto
import com.example.mobileapp.domain.model.Event

fun EventDto.toDomain(): Event = Event(id, userId, title, date, time, description, createdAt)
fun Event.toDto(): EventDto = EventDto(id, userId, title, date, time, description, createdAt)
