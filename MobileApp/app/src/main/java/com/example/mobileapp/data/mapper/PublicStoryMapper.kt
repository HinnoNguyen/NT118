package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.PublicStoryDto
import com.example.mobileapp.domain.model.PublicStory

fun PublicStoryDto.toDomain(): PublicStory = PublicStory(
    id = id,
    storyId = storyId,
    authorId = authorId,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    title = title,
    content = content,
    contentPreview = contentPreview,
    coverImageUrl = coverImageUrl,
    likeCount = likeCount,
    commentCount = commentCount,
    createdAt = createdAt,
    sharedAt = sharedAt,
    visibility = visibility
)

fun PublicStory.toDto(): PublicStoryDto = PublicStoryDto(
    id = id,
    storyId = storyId,
    authorId = authorId,
    authorName = authorName,
    authorAvatarUrl = authorAvatarUrl,
    title = title,
    content = content,
    contentPreview = contentPreview,
    coverImageUrl = coverImageUrl,
    likeCount = likeCount,
    commentCount = commentCount,
    createdAt = createdAt,
    sharedAt = sharedAt,
    visibility = visibility
)
