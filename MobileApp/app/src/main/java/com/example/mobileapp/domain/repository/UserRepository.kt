package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.User

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun getUserProfile(uid: String): Result<User>
    fun getCurrentUserId(): String?
    fun signOut()
}
