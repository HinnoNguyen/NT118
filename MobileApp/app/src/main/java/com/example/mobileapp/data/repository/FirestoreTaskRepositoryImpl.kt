package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.TaskDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
import com.example.mobileapp.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreTaskRepositoryImpl : TaskRepository {
    private val db = FirebaseFirestore.getInstance()

    private fun tasksCollection(userId: String) =
        db.collection("users").document(userId).collection("tasks")

    override suspend fun getTasks(userId: String): Resource<List<Task>> = try {
        val snapshot = tasksCollection(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Resource.Success(snapshot.documents.mapNotNull { it.toObject(TaskDto::class.java)?.toDomain() })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load tasks")
    }

    override suspend fun addTask(userId: String, title: String, description: String, dueAt: Long, priority: String): Resource<Task> = try {
        val docRef = tasksCollection(userId).document()
        val now = System.currentTimeMillis()
        val dto = TaskDto(id = docRef.id, userId = userId, title = title, description = description, dueAt = dueAt, completed = false, priority = priority, createdAt = now, updatedAt = now)
        docRef.set(dto).await()
        Resource.Success(dto.toDomain())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to add task")
    }

    override suspend fun toggleComplete(userId: String, taskId: String, completed: Boolean): Resource<Unit> = try {
        tasksCollection(userId).document(taskId)
            .update("completed", completed, "updatedAt", System.currentTimeMillis()).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to update task")
    }

    override suspend fun deleteTask(userId: String, taskId: String): Resource<Unit> = try {
        tasksCollection(userId).document(taskId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to delete task")
    }
}
