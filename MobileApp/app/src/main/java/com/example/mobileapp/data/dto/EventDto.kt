package com.example.mobileapp.data.dto

data class EventDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val date: Long = 0L,
    val time: String = "",
    val description: String = "",
    val createdAt: Long = 0L
)
