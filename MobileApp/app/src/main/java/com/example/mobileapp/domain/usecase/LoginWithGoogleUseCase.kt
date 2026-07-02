package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository

class LoginWithGoogleUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(idToken: String): Result<User> {
        return userRepository.signInWithGoogle(idToken)
    }
}
