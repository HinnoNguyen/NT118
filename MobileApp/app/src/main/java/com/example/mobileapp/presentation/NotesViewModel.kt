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
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            repository.getNotes(userId)
                .catch { e ->
                    _error.value = "Firestore Error: ${e.message}"
                    _isLoading.value = false
                }
                .collect {
                    _notes.value = it
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
            val now = System.currentTimeMillis()
            val newNote = Note(
                id = "",
                userId = userId,
                title = title,
                content = content,
                type = type,
                pinned = false,
                reminderTime = reminderTime,
                createdAt = now,
                updatedAt = now
            )
            repository.addNote(newNote).onFailure {
                _error.value = "Failed to save scroll"
            }
        }
    }
    
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId).onFailure {
                _error.value = "Failed to burn scroll"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
