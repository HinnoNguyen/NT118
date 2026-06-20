package com.example.mobileapp.domain.model

data class Story(
    val id: String,
    val userId: String,
    val title: String,
    val genre: String,
    val content: String,
    val relatedNoteIds: List<String>,
    val isPublic: Boolean,
    val sharedAt: Long,
    val coverImageUrl: String,
    val createdAt: Long,
    val updatedAt: Long
)
