package com.example.mobileapp.domain.usecase

import com.example.mobileapp.domain.model.User
import com.example.mobileapp.domain.repository.UserRepository
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setup() {
        repository = mock()
        loginUseCase = LoginUseCase(repository)
    }

    @Test
    fun `login with valid credentials returns success`() = runTest {
        val expectedUser = User("uid", "alice", "alice@example.com", "", 0)
        whenever(repository.login("alice@example.com", "password123"))
            .thenReturn(Resource.Success(expectedUser))

        val result = loginUseCase("alice@example.com", "password123")

        assertTrue(result is Resource.Success)
        assertEquals(expectedUser, (result as Resource.Success).data)
    }

    @Test
    fun `login with empty email returns error`() = runTest {
        val result = loginUseCase("", "password123")

        assertTrue(result is Resource.Error)
        assertEquals("Email cannot be empty.", (result as Resource.Error).message)
    }

    @Test
    fun `login with blank password returns error`() = runTest {
        val result = loginUseCase("alice@example.com", "")

        assertTrue(result is Resource.Error)
        assertEquals("Password cannot be empty.", (result as Resource.Error).message)
    }

    @Test
    fun `login with short password returns error`() = runTest {
        val result = loginUseCase("alice@example.com", "abc")

        assertTrue(result is Resource.Error)
        assertEquals("Password must be at least 6 characters.", (result as Resource.Error).message)
    }

    @Test
    fun `login delegates to repository when validation passes`() = runTest {
        whenever(repository.login("alice@example.com", "pass123"))
            .thenReturn(Resource.Error("Wrong password."))

        val result = loginUseCase("alice@example.com", "pass123")

        assertTrue(result is Resource.Error)
        assertEquals("Wrong password.", (result as Resource.Error).message)
    }
}
