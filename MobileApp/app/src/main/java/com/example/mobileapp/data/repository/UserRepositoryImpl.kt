package com.example.mobileapp.data.repository

import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.model.dto.UserDto
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.example.mobileapp.utils.Resource

/**
 * Concrete implementation of [UserRepository].
 * This is the **only** class in the project that knows about the actual
 * data source (currently a mock — ready to be replaced with Firebase/Retrofit).
 *
 * Lives in the data layer. The domain and presentation layers depend only on
 * the [UserRepository] interface, never on this class directly.
 */
class UserRepositoryImpl : UserRepository {

    // In-memory mock store — replace with Firebase Auth / Firestore calls.
    private val mockUsers = mutableMapOf<String, UserDto>()
    private var currentUser: UserDto? = null

    override suspend fun login(email: String, password: String): Resource<User> {
        return if (email.isNotBlank() && password.length >= 6) {
            val dto = mockUsers.values.firstOrNull { it.email == email }
                ?: UserDto(
                    uid = "mock_uid_${System.currentTimeMillis()}",
                    username = email.substringBefore("@"),
                    email = email,
                    photoUrl = "",
                    createdAt = System.currentTimeMillis()
                )
            currentUser = dto
            Resource.Success(dto.toDomain())
        } else {
            Resource.Error("Invalid credentials. Check your email and password.")
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): Resource<User> {
        return if (mockUsers.values.any { it.email == email }) {
            Resource.Error("An account with this email already exists.")
        } else {
            val dto = UserDto(
                uid = "mock_uid_${System.currentTimeMillis()}",
                username = username,
                email = email,
                photoUrl = "",
                createdAt = System.currentTimeMillis()
            )
            mockUsers[dto.uid] = dto
            currentUser = dto
            Resource.Success(dto.toDomain())
        }
    }

    override suspend fun getUserProfile(uid: String): Resource<User> {
        val dto = currentUser ?: mockUsers[uid]
        return if (dto != null) {
            Resource.Success(dto.toDomain())
        } else {
            Resource.Error("User profile not found.")
        }
    }

    override fun signOut() {
        currentUser = null
    }
}