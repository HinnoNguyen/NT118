package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.User

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun reloadCurrentUser(): Result<Unit>
    fun isCurrentUserEmailVerified(): Boolean
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun getCurrentUserId(): String?
    fun signOut()
    suspend fun awardExp(uid: String, amount: Int): Result<Unit>
    suspend fun addFocusMinutes(uid: String, minutes: Int): Result<Unit>
    suspend fun updateUserProfile(uid: String, name: String, avatarUrl: String, title: String, bio: String): Result<Unit>
}
