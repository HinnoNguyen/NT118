package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.TaskDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TaskRepositoryImpl : TaskRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val tasksCollection = firestore.collection("tasks")

    override suspend fun createTask(
        title: String,
        description: String,
        dueAt: Long,
        priority: String
    ): Result<Task> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (title.isBlank()) {
            return Result.failure(Exception("Task title cannot be empty"))
        }

        return try {
            val now = System.currentTimeMillis()
            val task = Task(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title.trim(),
                description = description.trim(),
                dueAt = dueAt,
                completed = false,
                priority = normalizePriority(priority),
                createdAt = now,
                updatedAt = now
            )

            tasksCollection.document(task.id).set(task.toDto()).await()
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to create task", e))
        }
    }

    override suspend fun getTasks(): Result<List<Task>> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))

        return try {
            val snapshot = tasksCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val tasks = snapshot.documents
                .mapNotNull { it.toObject(TaskDto::class.java)?.toDomain() }
                .sortedWith(
                    compareBy<Task> { it.completed }
                        .thenByDescending { priorityWeight(it.priority) }
                        .thenByDescending { it.updatedAt }
                )
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch tasks", e))
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        if (taskId.isBlank()) {
            return Result.failure(Exception("Task id cannot be empty"))
        }

        return try {
            val snapshot = tasksCollection.document(taskId).get().await()
            val task = snapshot.toObject(TaskDto::class.java)?.toDomain()
                ?: return Result.failure(Exception("Task not found"))
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to fetch task", e))
        }
    }

    override suspend fun updateTask(task: Task): Result<Task> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("No logged in user"))
        if (task.userId != userId) {
            return Result.failure(Exception("Cannot update another user's task"))
        }

        return try {
            val updatedTask = task.copy(
                title = task.title.trim(),
                description = task.description.trim(),
                priority = normalizePriority(task.priority),
                updatedAt = System.currentTimeMillis()
            )
            tasksCollection.document(updatedTask.id).set(updatedTask.toDto()).await()
            Result.success(updatedTask)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to update task", e))
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        if (taskId.isBlank()) {
            return Result.failure(Exception("Task id cannot be empty"))
        }

        return try {
            tasksCollection.document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to delete task", e))
        }
    }

    private fun normalizePriority(priority: String): String {
        return when (priority.lowercase()) {
            "high" -> "high"
            "low" -> "low"
            else -> "normal"
        }
    }

    private fun priorityWeight(priority: String): Int {
        return when (priority) {
            "high" -> 3
            "normal" -> 2
            else -> 1
        }
    }
}
