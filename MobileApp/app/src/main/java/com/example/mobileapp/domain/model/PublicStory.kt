package com.example.mobileapp.domain.model

data class PublicStory(
    val id: String,
    val storyId: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val title: String,
    val content: String,
    val contentPreview: String,
    val coverImageUrl: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Long,
    val sharedAt: Long,
    val visibility: String
)
