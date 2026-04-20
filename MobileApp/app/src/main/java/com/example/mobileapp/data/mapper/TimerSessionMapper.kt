package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.TimerSessionDto
import com.example.mobileapp.domain.model.TimerSession

fun TimerSessionDto.toDomain(): TimerSession = TimerSession(
    id = id,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt,
    durationMinutes = durationMinutes,
    completed = completed,
    createdAt = createdAt
)

fun TimerSession.toDto(): TimerSessionDto = TimerSessionDto(
    id = id,
    userId = userId,
    startedAt = startedAt,
    endedAt = endedAt,
    durationMinutes = durationMinutes,
    completed = completed,
    createdAt = createdAt
)
