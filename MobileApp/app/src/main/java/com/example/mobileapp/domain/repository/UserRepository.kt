package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.utils.Resource

/**
 * Contract for all user-related data operations.
 * Defined in the **domain** layer — nothing here imports Android or Firebase.
 * The data layer provides the concrete implementation.
 */
interface UserRepository {
    /** Authenticate with email and password. */
    suspend fun login(email: String, password: String): Resource<User>

    /** Create a new account with the given credentials. */
    suspend fun register(username: String, email: String, password: String): Resource<User>

    /** Fetch the profile of the currently signed-in user. */
    suspend fun getUserProfile(uid: String): Resource<User>

    /** Sign the current user out. */
    fun signOut()
}