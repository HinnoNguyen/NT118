package com.example.mobileapp.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the Notes screen.
 * Manages note-type selection and new-note-section visibility state.
 */
class NotesViewModel : ViewModel() {

    enum class NoteType { NOTE, REMINDER, FLASHCARD, NONE }

    private val _selectedType = MutableStateFlow(NoteType.NONE)
    val selectedType: StateFlow<NoteType> = _selectedType

    private val _isNewNoteVisible = MutableStateFlow(false)
    val isNewNoteVisible: StateFlow<Boolean> = _isNewNoteVisible

    private val _isReminderTimeVisible = MutableStateFlow(false)
    val isReminderTimeVisible: StateFlow<Boolean> = _isReminderTimeVisible

    fun selectType(type: NoteType) {
        _selectedType.value = type
        _isReminderTimeVisible.value = (type == NoteType.REMINDER)
    }

    fun toggleNewNoteSection() {
        _isNewNoteVisible.value = !_isNewNoteVisible.value
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                NotesViewModel() as T
        }
    }
}
