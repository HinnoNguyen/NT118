package com.example.mobileapp.data.repository

import com.example.mobileapp.data.dto.TaskDto
import com.example.mobileapp.data.mapper.toDomain
import com.example.mobileapp.data.mapper.toDto
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TaskRepository {

    private val tasksCollection = firestore.collection("tasks")

    override fun getTasks(userId: String): Flow<List<Task>> = callbackFlow {
        val registration = tasksCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tasks = snapshot.toObjects(TaskDto::class.java)
                        .map { it.toDomain() }
                        .sortedByDescending { it.createdAt }
                    trySend(tasks)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addTask(task: Task): Result<Unit> {
        return try {
            val docRef = tasksCollection.document()
            val taskWithId = task.copy(id = docRef.id)
            docRef.set(taskWithId.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            tasksCollection.document(task.id).set(task.toDto()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            tasksCollection.document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleTaskCompletion(taskId: String, completed: Boolean): Result<Unit> {
        return try {
            tasksCollection.document(taskId).update(
                "completed", completed,
                "updatedAt", System.currentTimeMillis()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
