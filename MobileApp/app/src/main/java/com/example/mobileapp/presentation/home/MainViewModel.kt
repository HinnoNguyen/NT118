package com.example.mobileapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.usecase.GetUserProfileUseCase
import com.example.mobileapp.domain.usecase.SignOutUseCase
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home (Main) screen.
 * Depends only on domain use cases.
 */
class MainViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<User>?>(null)
    val profileState: StateFlow<Resource<User>?> = _profileState

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            _profileState.value = getUserProfileUseCase(uid)
        }
    }

    fun signOut() = signOutUseCase()

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = UserRepositoryImpl()
                return MainViewModel(
                    GetUserProfileUseCase(repo),
                    SignOutUseCase(repo)
                ) as T
            }
        }
    }
}
