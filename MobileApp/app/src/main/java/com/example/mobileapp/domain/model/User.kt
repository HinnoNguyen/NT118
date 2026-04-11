package com.example.mobileapp.domain.model

/**
 * Pure domain entity for an authenticated user.
 * No Android or Firebase dependencies — this is the app's truth about a user.
 */
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)