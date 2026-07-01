package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mobileapp.data.repository.NoteRepositoryImpl
import com.example.mobileapp.data.repository.TaskRepositoryImpl
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.data.repository.NotificationRepositoryImpl
import com.example.mobileapp.domain.usecase.*

class ViewModelFactory : ViewModelProvider.Factory {
    
    private val userRepository = UserRepositoryImpl()
    private val taskRepository = TaskRepositoryImpl()
    private val noteRepository = NoteRepositoryImpl()
    private val notificationRepository = NotificationRepositoryImpl()
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val userId = userRepository.getCurrentUserId() ?: ""
        
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(LoginUseCase(userRepository)) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(RegisterUseCase(userRepository)) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userRepository, taskRepository, noteRepository) as T
            }
            modelClass.isAssignableFrom(QuestViewModel::class.java) -> {
                QuestViewModel(
                    GetTasksUseCase(taskRepository),
                    AddTaskUseCase(taskRepository),
                    ToggleTaskCompletionUseCase(taskRepository),
                    userRepository,
                    userId
                ) as T
            }
            modelClass.isAssignableFrom(NotesViewModel::class.java) -> {
                NotesViewModel(noteRepository, userId) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> {
                CalendarViewModel(taskRepository, userRepository, userId) as T
            }
            modelClass.isAssignableFrom(NotificationsViewModel::class.java) -> {
                NotificationsViewModel(notificationRepository, userId) as T
            }
            modelClass.isAssignableFrom(TimerViewModel::class.java) -> {
                TimerViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
