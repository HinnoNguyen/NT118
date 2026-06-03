package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.repository.UserRepository

class IsCurrentUserEmailVerifiedUseCase(private val userRepository: UserRepository) {
    fun execute(): Boolean {
        return userRepository.isCurrentUserEmailVerified()
    }
}
