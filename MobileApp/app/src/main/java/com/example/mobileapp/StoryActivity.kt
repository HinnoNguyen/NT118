package com.example.mobileapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.adapter.StoryAdapter
import com.example.mobileapp.adapter.StoryItem
import com.example.mobileapp.api.AiConstants
import com.example.mobileapp.api.GroqApiService
import com.example.mobileapp.api.GroqRequest
import com.example.mobileapp.api.Message
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StoryActivity : BaseActivity() {

    private var selectedGenre: String = "EPIC"
    private lateinit var storyAdapter: StoryAdapter
    private val storyList = mutableListOf<StoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        setupRecyclerView()
        setupStoryForge()
        setupNavigation()
    }

    private fun setupRecyclerView() {
        val rvStories = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvStories)
        storyAdapter = StoryAdapter(
            stories = storyList,
            onClick = { story -> showStoryDialog(story.content, story.title) },
            onDelete = { story -> deleteStory(story) }
        )
        rvStories.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvStories.adapter = storyAdapter
    }

    private fun setupStoryForge() {
        val btnCreateStory = findViewById<MaterialButton>(R.id.btnCreateStory)
        val newStorySection = findViewById<LinearLayout>(R.id.newStorySection)
        
        val genreEpic = findViewById<LinearLayout>(R.id.genreEpic)
        val genreMystery = findViewById<LinearLayout>(R.id.genreMystery)
        val genreComedy = findViewById<LinearLayout>(R.id.genreComedy)
        val genreHorror = findViewById<LinearLayout>(R.id.genreHorror)

        val etStoryInput = findViewById<EditText>(R.id.etStoryInput)
        val btnStoryfy = findViewById<MaterialButton>(R.id.btnStoryfy)

        val genres = listOf(genreEpic, genreMystery, genreComedy, genreHorror)

        // Toggle Forge Section
        btnCreateStory.setOnClickListener {
            if (newStorySection.visibility == View.GONE) {
                newStorySection.visibility = View.VISIBLE
                btnCreateStory.text = "✕"
            } else {
                newStorySection.visibility = View.GONE
                btnCreateStory.text = "+ CREATE"
            }
        }

        // Genre Selection Logic
        fun selectGenre(selected: LinearLayout, genreName: String) {
            selectedGenre = genreName
            genres.forEach { layout ->
                val text = layout.getChildAt(1) as TextView
                
                if (layout == selected) {
                    layout.setBackgroundColor(0xFFFFD700.toInt()) // Gold
                    text.setTextColor(ContextCompat.getColor(this, R.color.black))
                } else {
                    layout.setBackgroundResource(R.drawable.bg_button_unselected)
                    text.setTextColor(0xFFAAAAAA.toInt())
                }
            }
        }

        genreEpic.setOnClickListener { selectGenre(genreEpic, "EPIC") }
        genreMystery.setOnClickListener { selectGenre(genreMystery, "MYSTERY") }
        genreComedy.setOnClickListener { selectGenre(genreComedy, "COMEDY") }
        genreHorror.setOnClickListener { selectGenre(genreHorror, "HORROR") }

        btnStoryfy.setOnClickListener {
            val input = etStoryInput.text.toString()
            if (input.isNotBlank()) {
                generateStory(input)
            } else {
                Toast.makeText(this, "Please enter some information first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateStory(input: String) {
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val btnStoryfy = findViewById<MaterialButton>(R.id.btnStoryfy)

        // Groq API Configuration - Public key for easy cloning
        val apiKey = AiConstants.GROQ_API_KEY
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GroqApiService::class.java)

        val prompt = "Write a short $selectedGenre story based on these notes: $input. " +
                "Make it engaging and fitting the genre. Keep it under 200 words."

        val request = GroqRequest(
            messages = listOf(
                Message(role = "system", content = "You are a creative story writer."),
                Message(role = "user", content = prompt)
            )
        )

        lifecycleScope.launch {
            try {
                pbLoading.visibility = View.VISIBLE
                btnStoryfy.isEnabled = false
                
                val response = service.generateContent(apiKey, request)
                
                if (response.isSuccessful) {
                    val storyText = response.body()?.choices?.firstOrNull()?.message?.content
                    if (storyText != null) {
                        addNewStoryToList(storyText)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody?.contains("rate_limit") == true) {
                        Toast.makeText(this@StoryActivity, "Groq limit reached. Please wait a bit.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@StoryActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@StoryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("StoryActivity", "Groq Error: ", e)
            } finally {
                pbLoading.visibility = View.GONE
                btnStoryfy.isEnabled = true
            }
        }
    }

    private fun addNewStoryToList(content: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        // Extract a simple title from input or first words
        val etStoryInput = findViewById<EditText>(R.id.etStoryInput)
        val input = etStoryInput.text.toString()
        val title = if (input.length > 20) input.substring(0, 17) + "..." else input

        val newStory = StoryItem(
            id = UUID.randomUUID().toString(),
            genre = selectedGenre,
            title = title,
            content = content,
            date = currentDate
        )
        
        storyList.add(0, newStory)
        storyAdapter.notifyItemInserted(0)
        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvStories).scrollToPosition(0)
        
        showStoryDialog(content, title)
    }

    private fun deleteStory(story: StoryItem) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Story")
            .setMessage("Are you sure you want to delete this story?")
            .setPositiveButton("Delete") { _, _ ->
                val position = storyList.indexOf(story)
                if (position != -1) {
                    storyList.removeAt(position)
                    storyAdapter.notifyItemRemoved(position)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showStoryDialog(story: String, title: String = "Your Story") {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(story)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupNavigation() {
        // Handled by BaseActivity
    }
}
