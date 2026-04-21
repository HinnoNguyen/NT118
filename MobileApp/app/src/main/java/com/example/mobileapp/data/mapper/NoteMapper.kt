package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.NoteDto
import com.example.mobileapp.domain.model.Note

fun NoteDto.toDomain(): Note = Note(
    id = id,
    userId = userId,
    title = title,
    content = content,
    type = type,
    pinned = pinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toDto(): NoteDto = NoteDto(
    id = id,
    userId = userId,
    title = title,
    content = content,
    type = type,
    pinned = pinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)
