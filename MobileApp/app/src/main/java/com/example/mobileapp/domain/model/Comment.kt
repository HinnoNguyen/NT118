package com.example.mobileapp.domain.model

data class Comment(
    val id: String,
    val storyId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val createdAt: Long
)
