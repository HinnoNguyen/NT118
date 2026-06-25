package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.TaskDto
import com.example.mobileapp.domain.model.Task

fun TaskDto.toDomain(): Task = Task(
    id = id,
    userId = userId,
    title = title,
    description = description,
    dueAt = dueAt,
    completed = completed,
    priority = priority,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Task.toDto(): TaskDto = TaskDto(
    id = id,
    userId = userId,
    title = title,
    description = description,
    dueAt = dueAt,
    completed = completed,
    priority = priority,
    createdAt = createdAt,
    updatedAt = updatedAt
)
