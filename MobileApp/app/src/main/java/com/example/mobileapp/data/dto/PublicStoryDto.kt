package com.example.mobileapp.data.dto

data class PublicStoryDto(
    val id: String = "",
    val storyId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val title: String = "",
    val content: String = "",
    val contentPreview: String = "",
    val coverImageUrl: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long = 0L,
    val sharedAt: Long = 0L,
    val visibility: String = "public"
)
