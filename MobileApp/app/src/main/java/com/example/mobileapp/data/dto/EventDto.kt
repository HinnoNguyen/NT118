package com.example.mobileapp.data.dto

data class EventDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0L,
    val time: String = "",
    val location: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
