package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SnakeGameViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _expAwarded = MutableStateFlow<Int?>(null)
    val expAwarded: StateFlow<Int?> = _expAwarded.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserProfile(uid).onSuccess {
                _userProfile.value = it
            }
        }
    }

    fun awardExp(amount: Int) {
        if (amount <= 0) return
        
        val uid = userRepository.getCurrentUserId() ?: return
        
        viewModelScope.launch {
            val result = userRepository.awardExp(uid, amount, isTask = false)
            if (result.isSuccess) {
                _expAwarded.value = amount
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                if (errorMessage.contains("limit reached")) {
                    _error.value = errorMessage
                } else {
                    _error.value = "Failed to award EXP: $errorMessage"
                }
            }
        }
    }

    fun clearExpFlag() {
        _expAwarded.value = null
    }

    fun clearError() {
        _error.value = null
    }
}
