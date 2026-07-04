package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Task

interface TaskRepository {
    suspend fun createTask(
        title: String,
        description: String = "",
        dueAt: Long = 0L,
        priority: String = "normal"
    ): Result<Task>

    suspend fun getTasks(): Result<List<Task>>

    suspend fun getTask(taskId: String): Result<Task>

    suspend fun updateTask(task: Task): Result<Task>

    suspend fun deleteTask(taskId: String): Result<Unit>
}
