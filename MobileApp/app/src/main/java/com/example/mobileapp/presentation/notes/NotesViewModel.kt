package com.example.mobileapp.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.data.repository.FirestoreNoteRepositoryImpl
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.repository.NoteRepository
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    enum class NoteType { NOTE, REMINDER, FLASHCARD, NONE }

    private val _selectedType = MutableStateFlow(NoteType.NOTE)
    val selectedType: StateFlow<NoteType> = _selectedType

    private val _isNewNoteVisible = MutableStateFlow(false)
    val isNewNoteVisible: StateFlow<Boolean> = _isNewNoteVisible

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun selectType(type: NoteType) { _selectedType.value = type }

    fun toggleNewNoteSection() {
        _isNewNoteVisible.value = !_isNewNoteVisible.value
    }

    fun loadNotes(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = noteRepository.getNotes(userId)) {
                is Resource.Success -> _notes.value = result.data
                is Resource.Error   -> _error.value = result.message
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun saveNote(userId: String, title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        val type = _selectedType.value.name.lowercase()
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = noteRepository.addNote(userId, title, content, type)) {
                is Resource.Success -> {
                    _isNewNoteVisible.value = false
                    loadNotes(userId)
                }
                is Resource.Error -> _error.value = result.message
                is Resource.Loading -> {}
            }
            _isLoading.value = false
        }
    }

    fun deleteNote(userId: String, noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(userId, noteId)
            _notes.value = _notes.value.filter { it.id != noteId }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                NotesViewModel(FirestoreNoteRepositoryImpl()) as T
        }
    }
}
