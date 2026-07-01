package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _updateResult = MutableStateFlow<Result<Unit>?>(null)
    val updateResult: StateFlow<Result<Unit>?> = _updateResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProfile() {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = userRepository.getUserProfile(uid)
            result.onSuccess {
                _userProfile.value = it
            }.onFailure {
                _error.value = "Failed to load profile"
            }
        }
    }

    fun updateProfile(name: String, avatarUrl: String, title: String, bio: String) {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(uid, name, avatarUrl, title, bio)
            _updateResult.value = result
            if (result.isSuccess) {
                loadProfile()
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }
}
