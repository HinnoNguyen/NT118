package com.example.mobileapp.data.dto

data class TaskDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val dueAt: Long = 0L,
    val completed: Boolean = false,
    val priority: String = "normal",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
