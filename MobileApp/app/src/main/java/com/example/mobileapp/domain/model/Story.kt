package com.example.mobileapp.domain.model

data class Story(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val relatedNoteIds: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)
