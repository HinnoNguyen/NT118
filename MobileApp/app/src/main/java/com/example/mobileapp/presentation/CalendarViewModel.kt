package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
import com.example.mobileapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val userId: String
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            taskRepository.getTasks(userId)
                .catch { e ->
                    _error.value = "Failed to load tasks: ${e.message}"
                }
                .collect {
                    _tasks.value = it
                }
        }
    }

    fun addQuest(title: String, type: String, dueAt: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newTask = Task(
                id = "", // Will be set by repository/firestore
                userId = userId,
                title = title,
                description = type, // Use description to store type for calendar
                dueAt = dueAt,
                completed = false,
                priority = "MEDIUM",
                createdAt = now,
                updatedAt = now
            )
            val result = taskRepository.addTask(newTask)
            result.onFailure { e ->
                _error.value = "Failed to add quest: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
