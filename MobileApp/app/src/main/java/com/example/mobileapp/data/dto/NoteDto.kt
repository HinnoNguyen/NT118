package com.example.mobileapp.data.dto

data class NoteDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val type: String = "note",
    val pinned: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

