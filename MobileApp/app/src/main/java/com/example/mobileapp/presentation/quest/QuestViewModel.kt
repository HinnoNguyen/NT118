package com.example.mobileapp.presentation.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.FirestoreTaskRepositoryImpl
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isFormVisible = MutableStateFlow(false)
    val isFormVisible: StateFlow<Boolean> = _isFormVisible

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun toggleForm() { _isFormVisible.value = !_isFormVisible.value }

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            when (val result = taskRepository.getTasks(userId)) {
                is Resource.Success -> _tasks.value = result.data ?: emptyList()
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }
        }
    }

    fun addTask(userId: String, title: String, priority: String = "normal") {
        if (title.isBlank()) return
        viewModelScope.launch {
            when (val result = taskRepository.addTask(userId, title, "", 0L, priority)) {
                is Resource.Success -> {
                    _isFormVisible.value = false
                    loadTasks(userId)
                }
                is Resource.Error -> _error.value = result.message
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleComplete(userId: String, task: Task) {
        viewModelScope.launch {
            taskRepository.toggleComplete(userId, task.id, !task.completed)
            _tasks.value = _tasks.value.map {
                if (it.id == task.id) it.copy(completed = !it.completed) else it
            }
        }
    }

    fun deleteTask(userId: String, taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(userId, taskId)
            _tasks.value = _tasks.value.filter { it.id != taskId }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                QuestViewModel(FirestoreTaskRepositoryImpl()) as T
        }
    }
}
