package com.example.mobileapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.usecase.RegisterUseCase
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Register screen.
 * Depends only on [RegisterUseCase] — all validation lives in the use case.
 */
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            _registerState.value = registerUseCase(username, email, password, confirmPassword)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RegisterViewModel(RegisterUseCase(UserRepositoryImpl())) as T
        }
    }
}
