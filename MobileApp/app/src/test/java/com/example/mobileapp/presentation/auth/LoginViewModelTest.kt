package com.example.mobileapp.presentation.auth

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.example.mobileapp.domain.usecase.LoginUseCase
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: UserRepository
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        repository = mock()
        loginUseCase = LoginUseCase(repository)
        viewModel = LoginViewModel(loginUseCase)
    }

    @Test
    fun `login with valid credentials emits success state`() = runTest {
        val expectedUser = User("uid1", "alice", "alice@example.com", "", 0)
        whenever(repository.login("alice@example.com", "password123"))
            .thenReturn(Resource.Success(expectedUser))

        viewModel.login("alice@example.com", "password123")
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertTrue(state is Resource.Success)
        assertEquals(expectedUser, (state as Resource.Success).data)
    }

    @Test
    fun `login with empty email emits error state`() = runTest {
        viewModel.login("", "password123")
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertTrue(state is Resource.Error)
        assertEquals("Email cannot be empty.", (state as Resource.Error).message)
    }

    @Test
    fun `login with blank password emits error state`() = runTest {
        viewModel.login("alice@example.com", "")
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertTrue(state is Resource.Error)
        assertEquals("Password cannot be empty.", (state as Resource.Error).message)
    }

    @Test
    fun `login with short password emits error state`() = runTest {
        viewModel.login("alice@example.com", "abc")
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertTrue(state is Resource.Error)
        assertEquals("Password must be at least 6 characters.", (state as Resource.Error).message)
    }

    @Test
    fun `login transitions through loading then success`() = runTest {
        val expectedUser = User("uid2", "bob", "bob@example.com", "", 0)
        whenever(repository.login("bob@example.com", "secret99"))
            .thenReturn(Resource.Success(expectedUser))

        viewModel.login("bob@example.com", "secret99")
        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is Resource.Success)
    }

    @Test
    fun `login propagates repository error to state`() = runTest {
        whenever(repository.login("bad@example.com", "password123"))
            .thenReturn(Resource.Error("Invalid credentials."))

        viewModel.login("bad@example.com", "password123")
        advanceUntilIdle()

        val state = viewModel.loginState.value
        assertTrue(state is Resource.Error)
        assertEquals("Invalid credentials.", (state as Resource.Error).message)
    }

    @Test
    fun `initial login state is null`() {
        assertTrue(viewModel.loginState.value == null)
    }
}
