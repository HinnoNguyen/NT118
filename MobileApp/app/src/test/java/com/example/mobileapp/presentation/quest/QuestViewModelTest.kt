package com.example.mobileapp.presentation.quest

import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.domain.repository.TaskRepository
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
class QuestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var taskRepository: TaskRepository
    private lateinit var viewModel: QuestViewModel

    private val userId = "user123"

    private fun makeTask(id: String, title: String = "Task", completed: Boolean = false) = Task(
        id = id,
        userId = userId,
        title = title,
        description = "",
        dueAt = 0L,
        completed = completed,
        priority = "normal",
        createdAt = 0L,
        updatedAt = 0L
    )

    @Before
    fun setup() {
        taskRepository = mock()
        viewModel = QuestViewModel(taskRepository)
    }

    @Test
    fun `loadTasks on success populates tasks state`() = runTest {
        val tasks = listOf(makeTask("t1"), makeTask("t2"))
        whenever(taskRepository.getTasks(userId)).thenReturn(Resource.Success(tasks))

        viewModel.loadTasks(userId)
        advanceUntilIdle()

        assertEquals(tasks, viewModel.tasks.value)
    }

    @Test
    fun `loadTasks on error sets error state`() = runTest {
        whenever(taskRepository.getTasks(userId)).thenReturn(Resource.Error("Fetch failed"))

        viewModel.loadTasks(userId)
        advanceUntilIdle()

        assertEquals("Fetch failed", viewModel.error.value)
        assertTrue(viewModel.tasks.value.isEmpty())
    }

    @Test
    fun `addTask with valid title reloads tasks`() = runTest {
        val newTask = makeTask("t3", title = "Buy milk")
        whenever(taskRepository.addTask(userId, "Buy milk", "", 0L, "normal"))
            .thenReturn(Resource.Success(newTask))
        whenever(taskRepository.getTasks(userId)).thenReturn(Resource.Success(listOf(newTask)))

        viewModel.addTask(userId, "Buy milk")
        advanceUntilIdle()

        assertEquals(listOf(newTask), viewModel.tasks.value)
        assertFalse(viewModel.isFormVisible.value)
    }

    @Test
    fun `addTask with blank title does nothing`() = runTest {
        viewModel.addTask(userId, "   ")
        advanceUntilIdle()

        assertTrue(viewModel.tasks.value.isEmpty())
    }

    @Test
    fun `addTask on repository error sets error state`() = runTest {
        whenever(taskRepository.addTask(userId, "Read book", "", 0L, "normal"))
            .thenReturn(Resource.Error("Write error"))

        viewModel.addTask(userId, "Read book")
        advanceUntilIdle()

        assertEquals("Write error", viewModel.error.value)
    }

    @Test
    fun `toggleComplete flips task completed state locally`() = runTest {
        val task = makeTask("t1", completed = false)
        whenever(taskRepository.getTasks(userId)).thenReturn(Resource.Success(listOf(task)))
        whenever(taskRepository.toggleComplete(userId, "t1", true))
            .thenReturn(Resource.Success(Unit))

        viewModel.loadTasks(userId)
        advanceUntilIdle()

        viewModel.toggleComplete(userId, task)
        advanceUntilIdle()

        assertEquals(true, viewModel.tasks.value.first().completed)
    }

    @Test
    fun `deleteTask removes task from local state immediately`() = runTest {
        val tasks = listOf(makeTask("t1"), makeTask("t2"))
        whenever(taskRepository.getTasks(userId)).thenReturn(Resource.Success(tasks))
        whenever(taskRepository.deleteTask(userId, "t1")).thenReturn(Resource.Success(Unit))

        viewModel.loadTasks(userId)
        advanceUntilIdle()

        viewModel.deleteTask(userId, "t1")
        advanceUntilIdle()

        assertEquals(listOf(makeTask("t2")), viewModel.tasks.value)
    }

    @Test
    fun `toggleForm flips isFormVisible`() {
        assertFalse(viewModel.isFormVisible.value)
        viewModel.toggleForm()
        assertTrue(viewModel.isFormVisible.value)
        viewModel.toggleForm()
        assertFalse(viewModel.isFormVisible.value)
    }
}
