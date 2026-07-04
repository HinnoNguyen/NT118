package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.UserDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
                exp = 0,
                currentStreak = 1,
                bestStreak = 1,
                miniGameRewardCount = 0,
                lastMiniGameRewardAt = 0L
            )

            usersCollection.document(firebaseUser.uid).set(userDto).await()
            Result.success(userDto.toDomain())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Registration failed", e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Google sign-in failed"))
            
            val user = getOrCreateUserDocument(
                uid = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Hero",
                email = firebaseUser.email ?: ""
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            val userDto = snapshot.toObject(UserDto::class.java)
                ?: return Result.failure(Exception("User profile not found"))

            val now = System.currentTimeMillis()
            if (!isSameDay(userDto.updatedAt, now)) {
                // Handle streak
                val newStreak = if (isYesterday(userDto.updatedAt)) {
                    userDto.currentStreak + 1
                } else {
                    1
                }
                val newBestStreak = maxOf(newStreak, userDto.bestStreak)

                // Reset today stats if it's a new day
                usersCollection.document(uid).update(mapOf(
                    "todayFocusMinutes" to 0,
                    "miniGameRewardCount" to 0,
                    "currentStreak" to newStreak,
                    "bestStreak" to newBestStreak,
                    "updatedAt" to now
                )).await()
                Result.success(userDto.copy(
                    todayFocusMinutes = 0,
                    miniGameRewardCount = 0,
                    currentStreak = newStreak,
                    bestStreak = newBestStreak,
                    updatedAt = now
                ).toDomain())
            } else {
                Result.success(userDto.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch user profile", e))
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No logged in user"))
        return try {
            user.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to send verification email", e))
        }
    }

    override suspend fun reloadCurrentUser(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No logged in user"))
        return try {
            user.reload().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to reload current user", e))
        }
    }

    override fun isCurrentUserEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("Invalid email address"))
            }

            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Email does not exist", e))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to send password reset email", e))
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun signOut() {
        auth.signOut()
    }

    override fun getUserProfileFlow(uid: String): Flow<Result<User>> {
        return usersCollection.document(uid).snapshots().map { snapshot ->
            val dto = snapshot.toObject(UserDto::class.java)
            if (dto != null) {
                Result.success(dto.toDomain())
            } else {
                Result.failure(Exception("User not found"))
            }
        }
    }

    override suspend fun awardExp(uid: String, amount: Int, isTask: Boolean): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = usersCollection.document(uid)
                val snapshot = transaction.get(docRef)
                val userDto = snapshot.toObject(UserDto::class.java) 
                    ?: throw Exception("User profile not found during awardExp")

                val now = System.currentTimeMillis()
                
                val updateData = mutableMapOf<String, Any>(
                    "updatedAt" to now
                )

                if (!isTask && amount > 0) {
                    val isNewDay = !isSameDay(userDto.lastMiniGameRewardAt, now)
                    val currentCount = if (isNewDay) 0 else userDto.miniGameRewardCount
                    if (currentCount >= 3) {
                        throw Exception("Daily mini-game reward limit reached (max 3 times/day)")
                    }
                    updateData["miniGameRewardCount"] = currentCount + 1
                    updateData["lastMiniGameRewardAt"] = now
                }

                val newExp = userDto.exp + amount
                val newLevel = (newExp / 100) + 1
                
                updateData["exp"] = newExp
                updateData["level"] = newLevel

                if (isTask) {
                    val newCompletedCount = when {
                        amount > 0 -> userDto.completedTaskCount + 1
                        amount < 0 -> maxOf(0, userDto.completedTaskCount - 1)
                        else -> userDto.completedTaskCount
                    }
                    updateData["completedTaskCount"] = newCompletedCount
                }

                transaction.update(docRef, updateData)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFocusMinutes(uid: String, minutes: Int): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val docRef = usersCollection.document(uid)
                val snapshot = transaction.get(docRef)
                val userDto = snapshot.toObject(UserDto::class.java)
                    ?: throw Exception("User profile not found during addFocusMinutes")

                val now = System.currentTimeMillis()
                val isNewDay = !isSameDay(userDto.updatedAt, now)

                val newTotalFocus = userDto.totalFocusMinutes + minutes
                val newTodayFocus = if (isNewDay) minutes else userDto.todayFocusMinutes + minutes
                
                // Award EXP for focus minutes (1 EXP per minute)
                val newExp = userDto.exp + minutes
                val newLevel = (newExp / 100) + 1

                transaction.update(docRef, mapOf(
                    "totalFocusMinutes" to newTotalFocus,
                    "todayFocusMinutes" to newTodayFocus,
                    "exp" to newExp,
                    "level" to newLevel,
                    "updatedAt" to now
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(millis: Long): Boolean {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis
        return isSameDay(millis, yesterday)
    }

    override suspend fun updateUserProfile(uid: String, name: String, avatarUrl: String, title: String, bio: String): Result<Unit> {
        return try {
            usersCollection.document(uid).update(mapOf(
                "name" to name,
                "avatarUrl" to avatarUrl,
                "title" to title,
                "bio" to bio,
                "updatedAt" to System.currentTimeMillis()
            )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            exp = 0,
            currentStreak = 1,
            bestStreak = 1,
            miniGameRewardCount = 0,
            lastMiniGameRewardAt = 0L
        )
        userDocument.set(userDto).await()
        return userDto.toDomain()
    }
}
