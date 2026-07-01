package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(userId: String): Flow<List<Task>> = repository.getTasks(userId)
}

class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Unit> = repository.addTask(task)
}

class UpdateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Unit> = repository.updateTask(task)
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String): Result<Unit> = repository.deleteTask(taskId)
}

class ToggleTaskCompletionUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String, completed: Boolean): Result<Unit> = 
        repository.toggleTaskCompletion(taskId, completed)
}
