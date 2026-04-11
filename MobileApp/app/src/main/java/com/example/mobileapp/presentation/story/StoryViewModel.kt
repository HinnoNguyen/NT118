package com.example.mobileapp.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Story (StoryForge) screen.
 * Manages genre selection and forge-section visibility state.
 *
 * No use cases are needed here yet (no data-layer interaction),
 * but the ViewModel keeps all UI logic out of the Activity.
 */
class StoryViewModel : ViewModel() {

    enum class Genre { EPIC, MYSTERY, COMEDY, HORROR, NONE }

    private val _selectedGenre = MutableStateFlow(Genre.NONE)
    val selectedGenre: StateFlow<Genre> = _selectedGenre

    private val _isForgeVisible = MutableStateFlow(false)
    val isForgeVisible: StateFlow<Boolean> = _isForgeVisible

    fun selectGenre(genre: Genre) {
        _selectedGenre.value = genre
    }

    fun toggleForgeSection() {
        _isForgeVisible.value = !_isForgeVisible.value
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StoryViewModel() as T
        }
    }
}
