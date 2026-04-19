package com.example.mobileapp.data.repository

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import kotlinx.coroutines.delay

class UserRepositoryImpl : UserRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        // Mocking a network call
        return if (email.isNotEmpty() && password.length >= 6) {
            Result.success(User(email, "mock_token_123"))
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }
}