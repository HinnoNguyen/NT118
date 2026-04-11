package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.repository.UserRepository

/**
 * Use case: sign the current user out.
 */
class SignOutUseCase(private val userRepository: UserRepository) {
    operator fun invoke() = userRepository.signOut()
}
