package com.example.mobileapp.domain.model

data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val dueAt: Long,
    val completed: Boolean,
    val priority: String,
    val createdAt: Long,
    val updatedAt: Long
)
