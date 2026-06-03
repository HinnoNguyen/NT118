package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.repository.UserRepository

class SignOutUseCase(private val userRepository: UserRepository) {
    fun execute() {
        userRepository.signOut()
    }
}
