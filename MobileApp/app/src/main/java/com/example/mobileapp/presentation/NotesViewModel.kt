package com.example.mobileapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: NoteRepository,
    private val userId: String
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    
    private val _filterType = MutableStateFlow<String>("all")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(_notes, _filterType) { allNotes, filter ->
        if (filter == "all") {
            allNotes
        } else {
            allNotes.filter { it.type.lowercase() == filter.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getNotes()
                .onSuccess { list ->
                    _notes.value = list
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = "Firestore Error: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun setFilter(type: String) {
        _filterType.value = type
    }

    fun addNote(title: String, content: String, type: String = "note", reminderTime: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createNote(title = title, content = content, type = type, pinned = false, reminderTime = reminderTime)
                .onSuccess {
                    loadNotes()
                }
                .onFailure {
                    _error.value = "Failed to save scroll"
                }
        }
    }
    
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
                .onSuccess {
                    loadNotes()
                }
                .onFailure {
                    _error.value = "Failed to burn scroll"
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

