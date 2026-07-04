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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            taskRepository.getTasks()
                .onSuccess { list ->
                    _tasks.value = list
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = "Failed to load tasks: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun addQuest(title: String, type: String, dueAt: Long) {
        viewModelScope.launch {
            taskRepository.createTask(
                title = title,
                description = type, // Use description to store type for calendar
                dueAt = dueAt,
                priority = "normal"
            )
                .onSuccess {
                    loadTasks()
                }
                .onFailure { e ->
                    _error.value = "Failed to add quest: ${e.message}"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
