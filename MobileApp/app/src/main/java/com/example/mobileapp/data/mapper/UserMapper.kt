package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.dto.UserDto
import com.example.mobileapp.domain.model.User

fun UserDto.toDomain(): User = User(
    uid = uid,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    totalFocusMinutes = totalFocusMinutes,
    todayFocusMinutes = todayFocusMinutes,
    completedTaskCount = completedTaskCount,
    level = level,
    exp = exp
)

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    totalFocusMinutes = totalFocusMinutes,
    todayFocusMinutes = todayFocusMinutes,
    completedTaskCount = completedTaskCount,
    level = level,
    exp = exp
)
