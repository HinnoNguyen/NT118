package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository

class GetTasksUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(): Result<List<Task>> = repository.getTasks()
}

class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Task> = 
        repository.createTask(
            title = task.title,
            description = task.description,
            dueAt = task.dueAt,
            priority = task.priority
        )
}

class UpdateTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task): Result<Task> = repository.updateTask(task)
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String): Result<Unit> = repository.deleteTask(taskId)
}

class ToggleTaskCompletionUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(taskId: String, completed: Boolean): Result<Unit> {
        return repository.getTask(taskId).fold(
            onSuccess = { task ->
                repository.updateTask(task.copy(completed = completed)).map { Unit }
            },
            onFailure = { Result.failure(it) }
        )
    }
}

