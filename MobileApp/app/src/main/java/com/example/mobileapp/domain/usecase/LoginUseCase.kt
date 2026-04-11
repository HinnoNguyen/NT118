package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository

class LoginUseCase(private val userRepository: UserRepository) {
    suspend fun execute(email: String, password: String): Result<User> {
        return userRepository.login(email, password)
    }
}