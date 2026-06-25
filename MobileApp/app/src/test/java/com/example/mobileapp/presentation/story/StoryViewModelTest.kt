package com.example.mobileapp.presentation.story

import com.example.mobileapp.domain.model.Story
import com.example.mobileapp.domain.repository.StoryRepository
import com.example.mobileapp.util.MainDispatcherRule
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class StoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var storyRepository: StoryRepository
    private lateinit var viewModel: StoryViewModel

    private val userId = "user123"

    private fun makeStory(id: String, title: String = "My Story", genre: String = "epic") = Story(
        id = id,
        userId = userId,
        title = title,
        content = "Once upon a time...",
        genre = genre,
        relatedNoteIds = emptyList(),
        createdAt = 0L,
        updatedAt = 0L
    )

    @Before
    fun setup() {
        storyRepository = mock()
        viewModel = StoryViewModel(storyRepository)
    }

    @Test
    fun `loadStories on success populates stories state`() = runTest {
        val stories = listOf(makeStory("s1"), makeStory("s2"))
        whenever(storyRepository.getStories(userId)).thenReturn(Resource.Success(stories))

        viewModel.loadStories(userId)
        advanceUntilIdle()

        assertEquals(stories, viewModel.stories.value)
    }

    @Test
    fun `loadStories on error sets error state`() = runTest {
        whenever(storyRepository.getStories(userId)).thenReturn(Resource.Error("Fetch error"))

        viewModel.loadStories(userId)
        advanceUntilIdle()

        assertEquals("Fetch error", viewModel.error.value)
        assertTrue(viewModel.stories.value.isEmpty())
    }

    @Test
    fun `saveStory with valid content saves and reloads`() = runTest {
        val newStory = makeStory("s3", title = "Epic Story", genre = "epic")
        whenever(storyRepository.addStory(userId, "Epic Story", "Long ago...", "epic"))
            .thenReturn(Resource.Success(newStory))
        whenever(storyRepository.getStories(userId)).thenReturn(Resource.Success(listOf(newStory)))

        viewModel.selectGenre(StoryViewModel.Genre.EPIC)
        viewModel.saveStory(userId, "Epic Story", "Long ago...")
        advanceUntilIdle()

        assertEquals(listOf(newStory), viewModel.stories.value)
        assertFalse(viewModel.isForgeVisible.value)
    }

    @Test
    fun `saveStory with blank content does nothing`() = runTest {
        viewModel.saveStory(userId, "Title", "")
        advanceUntilIdle()

        assertTrue(viewModel.stories.value.isEmpty())
    }

    @Test
    fun `saveStory uses genre name as default title when title is blank`() = runTest {
        val newStory = makeStory("s4", title = "Mystery Story", genre = "mystery")
        whenever(storyRepository.addStory(userId, "Mystery Story", "Content here", "mystery"))
            .thenReturn(Resource.Success(newStory))
        whenever(storyRepository.getStories(userId)).thenReturn(Resource.Success(listOf(newStory)))

        viewModel.selectGenre(StoryViewModel.Genre.MYSTERY)
        viewModel.saveStory(userId, "", "Content here")
        advanceUntilIdle()

        assertEquals(listOf(newStory), viewModel.stories.value)
    }

    @Test
    fun `deleteStory removes story from local state immediately`() = runTest {
        val stories = listOf(makeStory("s1"), makeStory("s2"))
        whenever(storyRepository.getStories(userId)).thenReturn(Resource.Success(stories))
        whenever(storyRepository.deleteStory(userId, "s1")).thenReturn(Resource.Success(Unit))

        viewModel.loadStories(userId)
        advanceUntilIdle()

        viewModel.deleteStory(userId, "s1")
        advanceUntilIdle()

        assertEquals(listOf(makeStory("s2")), viewModel.stories.value)
    }

    @Test
    fun `selectGenre updates selectedGenre state`() {
        viewModel.selectGenre(StoryViewModel.Genre.HORROR)
        assertEquals(StoryViewModel.Genre.HORROR, viewModel.selectedGenre.value)
    }

    @Test
    fun `toggleForgeSection flips isForgeVisible`() {
        assertFalse(viewModel.isForgeVisible.value)
        viewModel.toggleForgeSection()
        assertTrue(viewModel.isForgeVisible.value)
        viewModel.toggleForgeSection()
        assertFalse(viewModel.isForgeVisible.value)
    }
}
