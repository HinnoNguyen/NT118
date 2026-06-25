package com.example.mobileapp.utils

import com.example.mobileapp.BuildConfig
import com.example.mobileapp.domain.model.Note
import com.google.ai.client.generativeai.GenerativeModel

object StoryAIHelper {

    private val model by lazy {
        GenerativeModel("gemini-1.5-flash", BuildConfig.GEMINI_API_KEY)
    }

    suspend fun suggestContinuation(currentText: String): String {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) return "Add GEMINI_API_KEY to local.properties to enable AI features."
        val prompt = """
            The user is writing a personal reflection story. Here is what they wrote so far:

            "$currentText"

            Suggest 2-3 thoughtful sentences to continue this reflection.
            Keep the first-person voice and their writing style.
            Be introspective and meaningful. Reply with only the suggested text, no preamble.
        """.trimIndent()
        return try {
            model.generateContent(prompt).text?.trim() ?: "Could not generate suggestion."
        } catch (e: Exception) {
            "AI unavailable: ${e.message}"
        }
    }

    suspend fun getWritingPrompts(currentText: String): List<String> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) return listOf("What did I learn?", "How did this make me feel?", "What would I do differently?")
        val prompt = """
            Based on this personal reflection:
            "$currentText"

            Generate exactly 3 short, thoughtful questions to help the writer reflect deeper.
            Reply with only the 3 questions, one per line, no numbering or bullets.
        """.trimIndent()
        return try {
            val response = model.generateContent(prompt).text ?: ""
            response.lines().map { it.trim() }.filter { it.isNotBlank() }.take(3)
                .ifEmpty { listOf("What did I learn?", "How did this make me feel?", "What's next for me?") }
        } catch (e: Exception) {
            listOf("What did I learn?", "How did this make me feel?", "What's next for me?")
        }
    }

    fun findRelatedNotes(currentText: String, allNotes: List<Note>): List<Note> {
        val keywords = currentText.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length > 4 }
            .take(8)
        return allNotes.filter { note ->
            keywords.any { kw ->
                note.title.lowercase().contains(kw) || note.content.lowercase().contains(kw)
            }
        }.take(5)
    }
}
