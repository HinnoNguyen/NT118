package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class StoryViewModel(
    private val storyRepository: StoryRepository,
    private val userId: String
) : ViewModel() {

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadStories()
    }

    private fun loadStories() {
        viewModelScope.launch {
            _isLoading.value = true
            storyRepository.getStories()
                .onSuccess { storyList ->
                    _stories.value = storyList
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = "Firestore Error: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun saveStory(title: String, genre: String, content: String) {
        viewModelScope.launch {
            storyRepository.createStory(title = title, genre = genre, content = content)
                .onSuccess {
                    loadStories()
                }
                .onFailure {
                    _error.value = "Failed to save story"
                }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            storyRepository.deleteStory(storyId)
                .onSuccess {
                    loadStories()
                }
                .onFailure {
                    _error.value = "Failed to delete story"
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
