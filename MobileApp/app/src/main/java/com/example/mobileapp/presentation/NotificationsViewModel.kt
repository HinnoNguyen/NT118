package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.AppNotification
import com.example.mobileapp.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationRepository,
    private val userId: String
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val unreadCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        if (userId.isEmpty()) {
            _error.value = "User not authenticated"
            return
        }
        viewModelScope.launch {
            repository.getNotifications(userId)
                .catch { e ->
                    _error.value = "Firestore Error: ${e.message}"
                }
                .collect {
                    _notifications.value = it
                }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAllNotifications(userId)
        }
    }
    
    fun addNotification(title: String, message: String, type: String) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            repository.addNotification(AppNotification(
                userId = userId,
                title = title,
                message = message,
                type = type
            ))
        }
    }
}
