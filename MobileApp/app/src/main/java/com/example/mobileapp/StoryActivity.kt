package com.example.mobileapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.adapter.StoryAdapter
import com.example.mobileapp.adapter.StoryItem
import com.example.mobileapp.api.AiConstants
import com.example.mobileapp.api.GroqApiService
import com.example.mobileapp.api.GroqRequest
import com.example.mobileapp.api.Message
import com.example.mobileapp.presentation.StoryViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoryActivity : BaseActivity() {

    private val viewModel: StoryViewModel by viewModels { ViewModelFactory() }
    private var selectedGenre: String = "EPIC"
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        setupRecyclerView()
        setupStoryForge()
        setupNavigation()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val rvStories = findViewById<RecyclerView>(R.id.rvStories)
        storyAdapter = StoryAdapter(
            onClick = { storyItem -> 
                val story = viewModel.stories.value.find { it.id == storyItem.id }
                story?.let { showStoryDialog(it) }
            },
            onDelete = { story -> deleteStory(story) }
        )
        rvStories.layoutManager = LinearLayoutManager(this)
        rvStories.adapter = storyAdapter
    }

    private fun setupStoryForge() {
        val btnCreateStory = findViewById<MaterialButton>(R.id.btnCreateStory)
        val newStorySection = findViewById<LinearLayout>(R.id.newStorySection)
        val btnStoryfy = findViewById<MaterialButton>(R.id.btnStoryfy)
        val etStoryTitleInput = findViewById<EditText>(R.id.etStoryTitleInput)
        val etStoryInput = findViewById<EditText>(R.id.etStoryInput)
        
        val genreEpic = findViewById<LinearLayout>(R.id.genreEpic)
        val genreMystery = findViewById<LinearLayout>(R.id.genreMystery)
        val genreComedy = findViewById<LinearLayout>(R.id.genreComedy)
        val genreHorror = findViewById<LinearLayout>(R.id.genreHorror)

        val genres = listOf(genreEpic, genreMystery, genreComedy, genreHorror)

        btnCreateStory.setOnClickListener {
            if (newStorySection.visibility == View.GONE) {
                newStorySection.visibility = View.VISIBLE
                newStorySection.alpha = 0f
                newStorySection.translationY = -20f
                newStorySection.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                btnCreateStory.text = "✕"
            } else {
                newStorySection.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(200)
                    .withEndAction { 
                        newStorySection.visibility = View.GONE 
                    }
                    .start()
                btnCreateStory.text = "+ CREATE"
            }
        }

        fun selectGenre(selected: LinearLayout, genreName: String) {
            selectedGenre = genreName
            genres.forEach { layout ->
                val text = layout.getChildAt(1) as TextView
                if (layout == selected) {
                    layout.setBackgroundColor(0xFFFFD700.toInt())
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

        findViewById<MaterialButton>(R.id.btnCommunity).setOnClickListener {
            navigateToCommunity()
        }

        btnStoryfy.setOnClickListener {
            val title = etStoryTitleInput.text.toString()
            val input = etStoryInput.text.toString()
            if (input.isNotBlank()) {
                generateStory(title, input)
            } else {
                showAppNotification("Attention", "Please enter some information first")
            }
        }
    }

    private fun generateStory(title: String, input: String) {
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val btnStoryfy = findViewById<MaterialButton>(R.id.btnStoryfy)
        val etStoryTitleInput = findViewById<EditText>(R.id.etStoryTitleInput)
        val etStoryInput = findViewById<EditText>(R.id.etStoryInput)
        
        val prompt = if (title.isNotBlank()) {
            "Write a short $selectedGenre story titled '$title' based on these notes: $input. Keep it under 200 words."
        } else {
            "Write a short $selectedGenre story based on these notes: $input. Keep it under 200 words."
        }

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
                
                val response = com.example.mobileapp.api.RetrofitClient.groqService.generateContent(AiConstants.GROQ_API_KEY, request)
                if (response.isSuccessful) {
                    val storyText = response.body()?.choices?.firstOrNull()?.message?.content
                    if (storyText != null) {
                        val finalTitle = title.ifBlank { input.take(20) }
                        addNewStoryToList(storyText, finalTitle)
                        etStoryTitleInput.text.clear()
                        etStoryInput.text.clear()
                    }
                } else {
                    val errorCode = response.code()
                    response.errorBody()?.close() // Fix: Close the error body to avoid resource leak
                    showAppNotification("API Error", "Code: $errorCode")
                }
            } catch (e: Exception) {
                showAppNotification("System Error", e.message ?: "Unknown error")
            } finally {
                pbLoading.visibility = View.GONE
                btnStoryfy.isEnabled = true
            }
        }
    }

    private fun addNewStoryToList(content: String, title: String) {
        val storyId = java.util.UUID.randomUUID().toString()
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val newStory = com.example.mobileapp.domain.model.Story(
            id = storyId,
            userId = userId,
            title = title,
            genre = selectedGenre,
            content = content,
            relatedNoteIds = emptyList(),
            isPublic = false,
            sharedAt = 0L,
            coverImageUrl = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModel.saveStory(newStory)
        showStoryDialog(newStory)
    }

    private fun deleteStory(story: StoryItem) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Story")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteStory(story.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { showLoading(it) }
                }
                launch {
                    viewModel.stories.collect { stories ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val items = stories.map {
                            StoryItem(
                                it.id,
                                it.genre,
                                it.title,
                                it.content,
                                dateFormat.format(Date(it.createdAt))
                            )
                        }
                        storyAdapter.updateStories(items)
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            showAppNotification("System Error", it)
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun showStoryDialog(story: com.example.mobileapp.domain.model.Story) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_story_detail)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<TextView>(R.id.tvDialogTitle).text = story.title.uppercase()
        dialog.findViewById<TextView>(R.id.tvDialogContent).text = story.content
        
        dialog.findViewById<MaterialButton>(R.id.btnShareToCommunity).setOnClickListener {
            val userName = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous Explorer"
            viewModel.publishStoryToCommunity(story, userName)
            dialog.dismiss()
            showAppNotification("Shared!", "Your story is now public in the community feed.")
        }

        dialog.findViewById<MaterialButton>(R.id.btnDialogClose).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun setupNavigation() {
        // Handled by BaseActivity
    }

    private fun navigateToCommunity() {
        startActivity(Intent(this, CommunityActivity::class.java))
    }
}
