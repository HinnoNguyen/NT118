package com.example.mobileapp.domain.repository

import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.utils.Resource

interface NoteRepository {
    suspend fun getNotes(userId: String): Resource<List<Note>>
    suspend fun addNote(userId: String, title: String, content: String, type: String): Resource<Note>
    suspend fun deleteNote(userId: String, noteId: String): Resource<Unit>
}
