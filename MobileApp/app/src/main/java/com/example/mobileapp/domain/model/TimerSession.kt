package com.example.mobileapp.domain.model

data class TimerSession(
    val id: String,
    val userId: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMinutes: Int,
    val completed: Boolean,
    val createdAt: Long
)
