package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.TimerSessionDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.TimerSession
import com.example.mobileapp.domain.repository.TimerSessionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TimerSessionRepositoryImpl : TimerSessionRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val timerSessionsCollection = firestore.collection("timer_sessions")

    override suspend fun createSession(durationMinutes: Int, startedAt: Long): Result<TimerSession> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (durationMinutes <= 0) {
            return Result.failure(Exception("Duration must be greater than zero"))
        }

        return try {
            val session = TimerSession(
                id = UUID.randomUUID().toString(),
                userId = userId,
                startedAt = startedAt,
                endedAt = 0L,
                durationMinutes = durationMinutes,
                completed = false,
                createdAt = startedAt
            )
            timerSessionsCollection.document(session.id).set(session.toDto()).await()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to create timer session", e))
        }
    }

    override suspend fun getSessions(limit: Long): Result<List<TimerSession>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))

        return try {
            val sessions = timerSessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(TimerSessionDto::class.java)?.toDomain() }
                .sortedByDescending { it.createdAt }
                .take(limit.toInt())
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch timer sessions", e))
        }
    }

    override suspend fun completeSession(session: TimerSession, endedAt: Long): Result<TimerSession> {
        return updateSession(session.copy(endedAt = endedAt, completed = true))
    }

    override suspend fun interruptSession(session: TimerSession, endedAt: Long): Result<TimerSession> {
        return updateSession(session.copy(endedAt = endedAt, completed = false))
    }

    private suspend fun updateSession(session: TimerSession): Result<TimerSession> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (session.userId != userId) {
            return Result.failure(Exception("Cannot update another user's timer session"))
        }

        return try {
            timerSessionsCollection.document(session.id).set(session.toDto()).await()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to update timer session", e))
        }
    }
}
