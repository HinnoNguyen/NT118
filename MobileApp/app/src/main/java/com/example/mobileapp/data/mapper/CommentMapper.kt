package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.CommentDto
import com.example.mobileapp.domain.model.Comment

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    storyId = storyId,
    userId = userId,
    userName = userName,
    content = content,
    createdAt = createdAt
)

fun Comment.toDto(): CommentDto = CommentDto(
    id = id,
    storyId = storyId,
    userId = userId,
    userName = userName,
    content = content,
    createdAt = createdAt
)
