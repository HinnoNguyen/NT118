package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val registerUseCase: RegisterUseCase) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                _registerState.value = RegisterState.Error("Please fill all fields")
                return@launch
            }

            if (password != confirmPassword) {
                _registerState.value = RegisterState.Error("Passwords do not match")
                return@launch
            }

            _registerState.value = RegisterState.Loading
            val result = registerUseCase.execute(name, email, password)
            result.onSuccess { user ->
                _registerState.value = RegisterState.Success(user)
            }.onFailure {
                _registerState.value = RegisterState.Error(it.message ?: "Registration failed")
            }
        }
    }

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val user: User) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }
}
