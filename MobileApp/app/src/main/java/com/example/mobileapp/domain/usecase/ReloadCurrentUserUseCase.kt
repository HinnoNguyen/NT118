package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.repository.UserRepository

class ReloadCurrentUserUseCase(private val userRepository: UserRepository) {
    suspend fun execute(): Result<Unit> {
        return userRepository.reloadCurrentUser()
    }
}
