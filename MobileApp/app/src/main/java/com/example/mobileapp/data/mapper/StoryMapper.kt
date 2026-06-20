package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.StoryDto
import com.example.mobileapp.domain.model.Story

fun StoryDto.toDomain(): Story = Story(
    id = id,
    userId = userId,
    title = title,
    content = content,
    relatedNoteIds = relatedNoteIds,
    isPublic = isPublic,
    sharedAt = sharedAt,
    coverImageUrl = coverImageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Story.toDto(): StoryDto = StoryDto(
    id = id,
    userId = userId,
    title = title,
    content = content,
    relatedNoteIds = relatedNoteIds,
    isPublic = isPublic,
    sharedAt = sharedAt,
    coverImageUrl = coverImageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)
