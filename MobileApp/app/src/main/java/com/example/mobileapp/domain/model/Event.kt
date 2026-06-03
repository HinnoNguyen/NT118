package com.example.mobileapp.domain.model

data class Event(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val date: Long,
    val time: String,
    val location: String,
    val createdAt: Long,
    val updatedAt: Long
)
