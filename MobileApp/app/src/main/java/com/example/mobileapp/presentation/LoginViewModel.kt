package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.usecase.LoginUseCase
import com.example.mobileapp.domain.usecase.LoginWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = loginUseCase.execute(email, pass)
            result.onSuccess { user ->
                _loginState.value = LoginState.Success(user)
            }.onFailure {
                _loginState.value = LoginState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = loginWithGoogleUseCase(idToken)
            result.onSuccess { user ->
                _loginState.value = LoginState.Success(user)
            }.onFailure {
                _loginState.value = LoginState.Error(it.message ?: "Google Login failed")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
