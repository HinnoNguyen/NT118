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

class RegisterUseCaseTest {

    private lateinit var repository: UserRepository
    private lateinit var registerUseCase: RegisterUseCase

    @Before
    fun setup() {
        repository = mock()
        registerUseCase = RegisterUseCase(repository)
    }

    @Test
    fun `register with valid data returns success`() = runTest {
        val expectedUser = User("uid", "alice", "alice@example.com", "", 0)
        whenever(repository.register("alice", "alice@example.com", "password123"))
            .thenReturn(Resource.Success(expectedUser))

        val result = registerUseCase("alice", "alice@example.com", "password123", "password123")

        assertTrue(result is Resource.Success)
        assertEquals(expectedUser, (result as Resource.Success).data)
    }

    @Test
    fun `register with blank username returns error`() = runTest {
        val result = registerUseCase("", "alice@example.com", "password123", "password123")

        assertTrue(result is Resource.Error)
        assertEquals("Username cannot be empty.", (result as Resource.Error).message)
    }

    @Test
    fun `register with blank email returns error`() = runTest {
        val result = registerUseCase("alice", "", "password123", "password123")

        assertTrue(result is Resource.Error)
        assertEquals("Email cannot be empty.", (result as Resource.Error).message)
    }

    @Test
    fun `register with blank password returns error`() = runTest {
        val result = registerUseCase("alice", "alice@example.com", "", "")

        assertTrue(result is Resource.Error)
        assertEquals("Password cannot be empty.", (result as Resource.Error).message)
    }

    @Test
    fun `register with password shorter than 6 chars returns error`() = runTest {
        val result = registerUseCase("alice", "alice@example.com", "abc", "abc")

        assertTrue(result is Resource.Error)
        assertEquals("Password must be at least 6 characters.", (result as Resource.Error).message)
    }

    @Test
    fun `register with mismatched passwords returns error`() = runTest {
        val result = registerUseCase("alice", "alice@example.com", "password123", "different")

        assertTrue(result is Resource.Error)
        assertEquals("Passwords do not match.", (result as Resource.Error).message)
    }

    @Test
    fun `register delegates to repository when all validation passes`() = runTest {
        whenever(repository.register("alice", "alice@example.com", "password123"))
            .thenReturn(Resource.Error("Email already in use."))

        val result = registerUseCase("alice", "alice@example.com", "password123", "password123")

        assertTrue(result is Resource.Error)
        assertEquals("Email already in use.", (result as Resource.Error).message)
    }
}
