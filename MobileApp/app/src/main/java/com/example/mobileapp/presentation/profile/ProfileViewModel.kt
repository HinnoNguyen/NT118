package com.example.mobileapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.usecase.GetUserProfileUseCase
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Profile screen.
 * Depends only on [GetUserProfileUseCase].
 */
class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<User>?>(null)
    val profileState: StateFlow<Resource<User>?> = _profileState

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            _profileState.value = getUserProfileUseCase(uid)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(GetUserProfileUseCase(UserRepositoryImpl())) as T
        }
    }
}
