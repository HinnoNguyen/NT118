package com.example.mobileapp.data.dto

data class StoryDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val relatedNoteIds: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
