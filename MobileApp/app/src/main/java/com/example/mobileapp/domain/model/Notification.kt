package com.example.mobileapp.domain.model

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general", // "calendar", "timer", "general"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
