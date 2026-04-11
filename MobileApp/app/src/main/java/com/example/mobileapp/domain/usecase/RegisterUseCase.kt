package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.example.mobileapp.utils.Resource

/**
 * Use case: register a new user account.
 * All registration validation rules live here — not in the ViewModel or Activity.
 */
class RegisterUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Resource<User> {
        return when {
            username.isBlank() -> Resource.Error("Username cannot be empty.")
            email.isBlank() -> Resource.Error("Email cannot be empty.")
            password.isBlank() -> Resource.Error("Password cannot be empty.")
            password.length < 6 -> Resource.Error("Password must be at least 6 characters.")
            password != confirmPassword -> Resource.Error("Passwords do not match.")
            else -> userRepository.register(username, email, password)
        }
    }
}
