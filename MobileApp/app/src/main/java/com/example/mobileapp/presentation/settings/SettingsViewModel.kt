package com.example.mobileapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * Exposes sign-out functionality via [SignOutUseCase].
 */
class SettingsViewModel(
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _signedOut.value = true
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(SignOutUseCase(UserRepositoryImpl())) as T
        }
    }
}
