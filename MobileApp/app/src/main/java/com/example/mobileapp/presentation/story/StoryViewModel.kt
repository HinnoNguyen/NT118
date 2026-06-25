package com.example.mobileapp.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.FirestoreStoryRepositoryImpl
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.StoryRepository
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StoryViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    enum class Genre { EPIC, MYSTERY, COMEDY, HORROR, NONE }

    private val _selectedGenre = MutableStateFlow(Genre.EPIC)
    val selectedGenre: StateFlow<Genre> = _selectedGenre

    private val _isForgeVisible = MutableStateFlow(false)
    val isForgeVisible: StateFlow<Boolean> = _isForgeVisible

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun selectGenre(genre: Genre) { _selectedGenre.value = genre }

    fun toggleForgeSection() { _isForgeVisible.value = !_isForgeVisible.value }

    fun loadStories(userId: String) {
        viewModelScope.launch {
            when (val result = storyRepository.getStories(userId)) {
                is Resource.Success -> _stories.value = result.data ?: emptyList()
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }
        }
    }

    fun saveStory(userId: String, title: String, content: String) {
        if (content.isBlank()) return
        val genre = _selectedGenre.value.name.lowercase()
        val finalTitle = title.ifBlank { "${genre.replaceFirstChar { it.uppercase() }} Story" }
        viewModelScope.launch {
            when (val result = storyRepository.addStory(userId, finalTitle, content, genre)) {
                is Resource.Success -> {
                    _isForgeVisible.value = false
                    loadStories(userId)
                }
                is Resource.Error -> _error.value = result.message
                is Resource.Loading -> {}
            }
        }
    }

    // TODO: inject NoteRepository to populate this; returns empty list for now so Related Notes degrades gracefully
    fun getLoadedNotes(): List<Note> = emptyList()

    fun deleteStory(userId: String, storyId: String) {
        viewModelScope.launch {
            storyRepository.deleteStory(userId, storyId)
            _stories.value = _stories.value.filter { it.id != storyId }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StoryViewModel(FirestoreStoryRepositoryImpl()) as T
        }
    }
}
