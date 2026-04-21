package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.repository.UserRepository

class SendPasswordResetEmailUseCase(private val userRepository: UserRepository) {
    suspend fun execute(email: String): Result<Unit> {
        return userRepository.sendPasswordResetEmail(email)
    }
}
