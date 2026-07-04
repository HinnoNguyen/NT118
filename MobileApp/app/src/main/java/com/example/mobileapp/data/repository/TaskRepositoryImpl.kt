package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.TaskDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TaskRepositoryImpl : TaskRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val tasksCollection = firestore.collection("tasks")

    override fun getTasks(userId: String): Flow<List<Task>> {
        return tasksCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toObject(TaskDto::class.java)?.toDomain() }
                    .sortedWith(
                        compareBy<Task> { it.completed }
                            .thenByDescending { priorityWeight(it.priority) }
                            .thenByDescending { it.updatedAt }
                    )
            }
    }

    override suspend fun addTask(task: Task): Result<Unit> {
        return try {
            val finalId = if (task.id.isBlank()) tasksCollection.document().id else task.id
            val finalTask = if (task.id.isBlank()) task.copy(id = finalId) else task
            
            tasksCollection.document(finalId).set(finalTask.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to add task", e))
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

    override suspend fun updateTask(task: Task): Result<Unit> {
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
            Result.success(Unit)
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

    override suspend fun toggleTaskCompletion(taskId: String, completed: Boolean): Result<Unit> {
        return try {
            tasksCollection.document(taskId).update("completed", completed, "updatedAt", System.currentTimeMillis()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to toggle task completion", e))
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
