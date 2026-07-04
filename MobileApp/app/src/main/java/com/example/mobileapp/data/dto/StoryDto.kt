package com.example.mobileapp.data.dto

data class StoryDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val genre: String = "",
    val content: String = "",
    val relatedNoteIds: List<String> = emptyList(),
    val isPublic: Boolean = false,
    val sharedAt: Long = 0L,
    val coverImageUrl: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
