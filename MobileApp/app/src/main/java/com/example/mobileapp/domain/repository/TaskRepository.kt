package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(userId: String): Flow<List<Task>>

    suspend fun addTask(task: Task): Result<Unit>

    suspend fun getTask(taskId: String): Result<Task>

    suspend fun updateTask(task: Task): Result<Unit>

    suspend fun deleteTask(taskId: String): Result<Unit>

    suspend fun toggleTaskCompletion(taskId: String, completed: Boolean): Result<Unit>
}
