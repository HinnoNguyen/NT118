package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.User

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
}