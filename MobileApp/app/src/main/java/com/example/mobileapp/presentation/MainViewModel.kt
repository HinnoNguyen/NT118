package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.NoteRepository
import com.example.mobileapp.domain.repository.TaskRepository
import com.example.mobileapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _notesCount = MutableStateFlow(0)
    val notesCount: StateFlow<Int> = _notesCount.asStateFlow()

    private val _todayTasksCompleted = MutableStateFlow(0)
    val todayTasksCompleted: StateFlow<Int> = _todayTasksCompleted.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Derived state for daily quest summary
    val remainingQuests: StateFlow<Int> = combine(
        _userProfile,
        _notesCount,
        _todayTasksCompleted
    ) { profile, notes, tasks ->
        var completedCount = 0
        if (tasks >= 3) completedCount++
        if ((profile?.todayFocusMinutes ?: 0) >= 30) completedCount++
        if (notes > 0) completedCount++ // Simplified: checking if any note exists
        
        3 - completedCount
    }.stateIn(viewModelScope, SharingStarted.Lazily, 3)

    fun loadData() {
        val uid = userRepository.getCurrentUserId() ?: return
        
        // Ensure we don't start multiple collectors if loadData is called again
        // However, ViewModelScope will clean them up on clear.
        // For MainViewModel, loadData is usually called once.
        
        startObserving(uid)
    }

    private fun startObserving(uid: String) {
        // Start observe user profile
        viewModelScope.launch {
            userRepository.getUserProfileFlow(uid)
                .onStart { _isLoading.value = true }
                .catch { e -> 
                    _error.value = "Profile Error: ${e.message}"
                    _isLoading.value = false
                }
                .collect { result ->
                    result.onSuccess { _userProfile.value = it }
                    _isLoading.value = false
                }
        }

        // Start observe notes count
        viewModelScope.launch {
            noteRepository.getNotes(uid)
                .catch { e -> _error.value = "Notes Error: ${e.message}" }
                .collect { notes -> _notesCount.value = notes.size }
        }

        // Start observe today tasks
        viewModelScope.launch {
            taskRepository.getTasks(uid)
                .catch { e -> _error.value = "Tasks Error: ${e.message}" }
                .collect { tasks ->
                    val startOfDay = getStartOfDay()
                    val todayCompleted = tasks.count { it.completed && it.updatedAt >= startOfDay }
                    _todayTasksCompleted.value = todayCompleted
                }
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun clearError() {
        _error.value = null
    }
}
