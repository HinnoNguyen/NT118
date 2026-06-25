package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.utils.Resource

interface TaskRepository {
    suspend fun getTasks(userId: String): Resource<List<Task>>
    suspend fun addTask(userId: String, title: String, description: String, dueAt: Long, priority: String): Resource<Task>
    suspend fun toggleComplete(userId: String, taskId: String, completed: Boolean): Resource<Unit>
    suspend fun deleteTask(userId: String, taskId: String): Resource<Unit>
}
