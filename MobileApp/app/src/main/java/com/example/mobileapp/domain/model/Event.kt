package com.example.mobileapp.domain.model

data class Event(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val date: Long = 0L,       // day timestamp (midnight of selected day)
    val time: String = "",     // "HH:mm"
    val description: String = "",
    val createdAt: Long = 0L
)
