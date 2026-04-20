package com.example.mobileapp.data.dto

data class TimerSessionDto(
    val id: String = "",
    val userId: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val durationMinutes: Int = 0,
    val completed: Boolean = false,
    val createdAt: Long = 0L
)
