package com.example.mobileapp.domain.model

data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val type: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
