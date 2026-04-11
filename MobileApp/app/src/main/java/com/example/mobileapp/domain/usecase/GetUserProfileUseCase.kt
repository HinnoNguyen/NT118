package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.example.mobileapp.utils.Resource

/**
 * Use case: fetch the profile of the currently signed-in user.
 */
class GetUserProfileUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(uid: String): Resource<User> =
        userRepository.getUserProfile(uid)
}
