package com.example.mobileapp.presentation.auth

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.example.mobileapp.domain.usecase.RegisterUseCase
import com.example.mobileapp.util.MainDispatcherRule
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: UserRepository
    private lateinit var registerUseCase: RegisterUseCase
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        repository = mock()
        registerUseCase = RegisterUseCase(repository)
        viewModel = RegisterViewModel(registerUseCase)
    }

    @Test
    fun `register with valid data emits success state`() = runTest {
        val expectedUser = User("uid1", "alice", "alice@example.com", "", 0)
        whenever(repository.register("alice", "alice@example.com", "password123"))
            .thenReturn(Resource.Success(expectedUser))

        viewModel.register("alice", "alice@example.com", "password123", "password123")
        advanceUntilIdle()

        val state = viewModel.registerState.value
        assertTrue(state is Resource.Success)
        assertEquals(expectedUser, (state as Resource.Success).data)
    }

    @Test
    fun `register with blank username emits error`() = runTest {
        viewModel.register("", "alice@example.com", "password123", "password123")
        advanceUntilIdle()

        val state = viewModel.registerState.value
        assertTrue(state is Resource.Error)
        assertEquals("Username cannot be empty.", (state as Resource.Error).message)
    }

    @Test
    fun `register with blank email emits error`() = runTest {
        viewModel.register("alice", "", "password123", "password123")
        advanceUntilIdle()

        val state = viewModel.registerState.value
        assertTrue(state is Resource.Error)
        assertEquals("Email cannot be empty.", (state as Resource.Error).message)
    }

    @Test
    fun `register with short password emits error`() = runTest {
        viewModel.register("alice", "alice@example.com", "abc", "abc")
        advanceUntilIdle()

        val state = viewModel.registerState.value
        assertTrue(state is Resource.Error)
        assertEquals("Password must be at least 6 characters.", (state as Resource.Error).message)
    }

    @Test
    fun `register with mismatched passwords emits error`() = runTest {
        viewModel.register("alice", "alice@example.com", "password123", "different")
        advanceUntilIdle()

        val state = viewModel.registerState.value
        assertTrue(state is Resource.Error)
        assertEquals("Passwords do not match.", (state as Resource.Error).message)
    }

    @Test
    fun `register propagates repository error to state`() = runTest {
        whenever(repository.register("alice", "alice@example.com", "password123"))
            .thenReturn(Resource.Error("Email already in use."))

        viewModel.register("alice", "alice@example.com", "password123", "password123")
        advanceUntilIdle()

        val state = viewModel.registerState.value
        assertTrue(state is Resource.Error)
        assertEquals("Email already in use.", (state as Resource.Error).message)
    }

    @Test
    fun `initial register state is null`() {
        assertTrue(viewModel.registerState.value == null)
    }
}
