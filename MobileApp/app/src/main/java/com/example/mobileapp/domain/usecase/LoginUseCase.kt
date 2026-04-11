package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.example.mobileapp.utils.Resource

/**
 * Use case: authenticate a user with email and password.
 * Owns input validation so the ViewModel stays thin.
 */
class LoginUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) return Resource.Error("Email cannot be empty.")
        if (password.isBlank()) return Resource.Error("Password cannot be empty.")
        if (password.length < 6) return Resource.Error("Password must be at least 6 characters.")
        return userRepository.login(email, password)
    }
}