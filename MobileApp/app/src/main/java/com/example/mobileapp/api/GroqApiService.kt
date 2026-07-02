package com.example.mobileapp.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("v1/chat/completions")
    suspend fun generateContent(
        @Header("Authorization") apiKey: String,
        @Body request: GroqRequest
    ): Response<GroqResponse>
}

data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<Message>
)

data class Message(val role: String, val content: String)

data class GroqResponse(val choices: List<Choice>)
data class Choice(val message: Message)
