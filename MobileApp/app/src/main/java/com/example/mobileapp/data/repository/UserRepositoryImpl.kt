package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.UserDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (email.isBlank() || password.length < 6) {
                return Result.failure(Exception("Invalid email or password"))
            }

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed"))
            val user = getOrCreateUserDocument(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: email.substringBefore("@"),
                email = firebaseUser.email ?: email
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Login failed", e))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        return try {
            if (name.isBlank()) {
                return Result.failure(Exception("Name cannot be empty"))
            }
            if (email.isBlank() || password.length < 6) {
                return Result.failure(Exception("Invalid email or password"))
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Registration failed"))
            val now = System.currentTimeMillis()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            val userDto = UserDto(
                uid = firebaseUser.uid,
                name = name,
                email = firebaseUser.email ?: email,
                avatarUrl = "",
                createdAt = now,
                updatedAt = now,
                totalFocusMinutes = 0,
                todayFocusMinutes = 0,
                completedTaskCount = 0,
                level = 1,
                exp = 0
            )

            usersCollection.document(firebaseUser.uid).set(userDto).await()
            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration failed", e))
        }
    }

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val userDto = snapshot.toObject(UserDto::class.java)
                ?: return Result.failure(Exception("User profile not found"))

            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch user profile", e))
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun signOut() {
        auth.signOut()
    }

    private suspend fun getOrCreateUserDocument(
        uid: String,
        name: String,
        email: String
    ): User {
        val userDocument = usersCollection.document(uid)
        val snapshot = userDocument.get().await()
        val existingUser = snapshot.toObject(UserDto::class.java)
        if (existingUser != null) {
            return existingUser.toDomain()
        }

        val now = System.currentTimeMillis()
        val userDto = UserDto(
            uid = uid,
            name = name,
            email = email,
            avatarUrl = "",
            createdAt = now,
            updatedAt = now,
            totalFocusMinutes = 0,
            todayFocusMinutes = 0,
            completedTaskCount = 0,
            level = 1,
            exp = 0
        )
        userDocument.set(userDto).await()
        return userDto.toDomain()
    }
}
