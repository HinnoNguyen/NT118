package com.example.mobileapp.domain.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val avatarUrl: String,
    val title: String = "",
    val bio: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val totalFocusMinutes: Int,
    val todayFocusMinutes: Int,
    val completedTaskCount: Int,
    val level: Int,
    val exp: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val miniGameRewardCount: Int = 0,
    val lastMiniGameRewardAt: Long = 0L
)