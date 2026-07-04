package com.example.mobileapp.data.dto

data class CommentDto(
    val id: String = "",
    val storyId: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val createdAt: Long = 0L
)
