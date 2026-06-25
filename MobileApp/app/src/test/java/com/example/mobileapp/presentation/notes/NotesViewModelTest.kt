package com.example.mobileapp.presentation.notes

import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.domain.repository.NoteRepository
import com.example.mobileapp.util.MainDispatcherRule
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var noteRepository: NoteRepository
    private lateinit var viewModel: NotesViewModel

    private val userId = "user123"

    private fun makeNote(id: String, title: String = "Title", type: String = "note") = Note(
        id = id,
        userId = userId,
        title = title,
        content = "Content",
        type = type,
        pinned = false,
        createdAt = 0L,
        updatedAt = 0L
    )

    @Before
    fun setup() {
        noteRepository = mock()
        viewModel = NotesViewModel(noteRepository)
    }

    @Test
    fun `loadNotes on success populates notes state`() = runTest {
        val notes = listOf(makeNote("n1"), makeNote("n2"))
        whenever(noteRepository.getNotes(userId)).thenReturn(Resource.Success(notes))

        viewModel.loadNotes(userId)
        advanceUntilIdle()

        assertEquals(notes, viewModel.notes.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadNotes on error sets error state`() = runTest {
        whenever(noteRepository.getNotes(userId)).thenReturn(Resource.Error("Network failure"))

        viewModel.loadNotes(userId)
        advanceUntilIdle()

        assertEquals("Network failure", viewModel.error.value)
        assertTrue(viewModel.notes.value.isEmpty())
    }

    @Test
    fun `saveNote with valid data reloads notes`() = runTest {
        val newNote = makeNote("n3", title = "New Note")
        whenever(noteRepository.addNote(userId, "New Note", "Body", "note"))
            .thenReturn(Resource.Success(newNote))
        whenever(noteRepository.getNotes(userId))
            .thenReturn(Resource.Success(listOf(newNote)))

        viewModel.saveNote(userId, "New Note", "Body")
        advanceUntilIdle()

        assertEquals(listOf(newNote), viewModel.notes.value)
        assertFalse(viewModel.isNewNoteVisible.value)
    }

    @Test
    fun `saveNote with blank title and content does nothing`() = runTest {
        viewModel.saveNote(userId, "", "")
        advanceUntilIdle()

        assertTrue(viewModel.notes.value.isEmpty())
    }

    @Test
    fun `saveNote on repository error sets error state`() = runTest {
        whenever(noteRepository.addNote(userId, "Title", "Body", "note"))
            .thenReturn(Resource.Error("Write failed"))

        viewModel.saveNote(userId, "Title", "Body")
        advanceUntilIdle()

        assertEquals("Write failed", viewModel.error.value)
    }

    @Test
    fun `deleteNote removes note from local state immediately`() = runTest {
        val notes = listOf(makeNote("n1"), makeNote("n2"))
        whenever(noteRepository.getNotes(userId)).thenReturn(Resource.Success(notes))
        whenever(noteRepository.deleteNote(userId, "n1")).thenReturn(Resource.Success(Unit))

        viewModel.loadNotes(userId)
        advanceUntilIdle()

        viewModel.deleteNote(userId, "n1")
        advanceUntilIdle()

        assertEquals(listOf(makeNote("n2")), viewModel.notes.value)
    }

    @Test
    fun `toggleNewNoteSection flips isNewNoteVisible`() {
        assertFalse(viewModel.isNewNoteVisible.value)
        viewModel.toggleNewNoteSection()
        assertTrue(viewModel.isNewNoteVisible.value)
        viewModel.toggleNewNoteSection()
        assertFalse(viewModel.isNewNoteVisible.value)
    }

    @Test
    fun `selectType updates selectedType state`() {
        viewModel.selectType(NotesViewModel.NoteType.FLASHCARD)
        assertEquals(NotesViewModel.NoteType.FLASHCARD, viewModel.selectedType.value)
    }
}
