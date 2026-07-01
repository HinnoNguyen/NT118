package com.example.mobileapp.data.dto

data class UserDto(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val totalFocusMinutes: Int = 0,
    val todayFocusMinutes: Int = 0,
    val completedTaskCount: Int = 0,
    val level: Int = 1,
    val exp: Int = 0
)