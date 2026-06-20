package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.TimerSession

interface TimerSessionRepository {
    suspend fun createSession(durationMinutes: Int, startedAt: Long = System.currentTimeMillis()): Result<TimerSession>

    suspend fun getSessions(limit: Long = 20): Result<List<TimerSession>>

    suspend fun completeSession(session: TimerSession, endedAt: Long = System.currentTimeMillis()): Result<TimerSession>

    suspend fun interruptSession(session: TimerSession, endedAt: Long = System.currentTimeMillis()): Result<TimerSession>
}
