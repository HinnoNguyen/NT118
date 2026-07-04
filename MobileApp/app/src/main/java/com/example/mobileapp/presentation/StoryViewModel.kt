package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.PublicStoryRepository
import com.example.mobileapp.domain.repository.StoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class StoryViewModel(
    private val storyRepository: StoryRepository,
    private val publicStoryRepository: PublicStoryRepository,
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
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            storyRepository.getStories(userId)
                .catch { e ->
                    _error.value = "Firestore Error: ${e.message}"
                    _isLoading.value = false
                }
                .collect { storyList ->
                    _stories.value = storyList
                    _isLoading.value = false
                }
        }
    }

    fun saveStory(story: Story) {
        viewModelScope.launch {
            val result = storyRepository.saveStory(story)
            if (result.isFailure) {
                _error.value = "Failed to save story"
            }
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            val result = storyRepository.deleteStory(storyId)
            if (result.isFailure) {
                _error.value = "Failed to delete story"
            }
        }
    }

    fun publishStoryToCommunity(story: Story, authorName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = publicStoryRepository.publishStory(story, authorName)
            if (result.isSuccess) {
                // Update local story status if needed
                storyRepository.updateStory(story.copy(isPublic = true, sharedAt = System.currentTimeMillis()))
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to publish story"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
