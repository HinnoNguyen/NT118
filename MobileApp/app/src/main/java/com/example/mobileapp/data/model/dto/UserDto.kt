package com.example.mobileapp.data.model.dto

/**
 * Data-layer representation of a user, used when communicating with
 * a remote or local data source (e.g. backend API, Room, Firebase).
 * Isolated from domain so the data source can evolve independently.
 */
data class UserDto(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L
)
