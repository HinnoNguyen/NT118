package com.example.mobileapp.data.mapper

import com.example.mobileapp.data.model.dto.UserDto
import com.example.mobileapp.domain.model.User

/** Maps a [UserDto] (data layer) to a [User] domain entity. */
fun UserDto.toDomain(): User = User(
    uid = uid,
    username = username,
    email = email,
    photoUrl = photoUrl,
    createdAt = createdAt
)

/** Maps a [User] domain entity to a [UserDto] for storage / transmission. */
fun User.toDto(): UserDto = UserDto(
    uid = uid,
    username = username,
    email = email,
    photoUrl = photoUrl,
    createdAt = createdAt
)
