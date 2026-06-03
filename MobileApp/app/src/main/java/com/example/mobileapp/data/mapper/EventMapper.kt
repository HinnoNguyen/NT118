package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.EventDto
import com.example.mobileapp.domain.model.Event

fun EventDto.toDomain(): Event = Event(
    id = id,
    userId = userId,
    title = title,
    description = description,
    date = date,
    time = time,
    location = location,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Event.toDto(): EventDto = EventDto(
    id = id,
    userId = userId,
    title = title,
    description = description,
    date = date,
    time = time,
    location = location,
    createdAt = createdAt,
    updatedAt = updatedAt
)
