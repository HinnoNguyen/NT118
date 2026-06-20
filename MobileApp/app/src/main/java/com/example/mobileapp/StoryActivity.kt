package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.PublicStoryRepositoryImpl
import com.example.mobileapp.data.repository.StoryRepositoryImpl
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.model.Story
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoryActivity : AppCompatActivity() {
    private val storyRepository = StoryRepositoryImpl()
    private val publicStoryRepository = PublicStoryRepositoryImpl()
    private val userRepository = UserRepositoryImpl()

    private lateinit var btnCreateStory: MaterialButton
    private lateinit var btnCommunity: MaterialButton
    private lateinit var newStorySection: LinearLayout
    private lateinit var genreEpic: LinearLayout
    private lateinit var genreMystery: LinearLayout
    private lateinit var genreComedy: LinearLayout
    private lateinit var genreHorror: LinearLayout
    private lateinit var etStoryContent: EditText
    private lateinit var btnStoryfy: MaterialButton
    private lateinit var tvStoriesStatus: TextView
    private lateinit var storiesListContainer: LinearLayout

    private var selectedGenre = "epic"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        setupStoryForge()
        setupNavigation()
    }

    private fun setupStoryForge() {
        btnCreateStory = findViewById(R.id.btnCreateStory)
        btnCommunity = findViewById(R.id.btnCommunity)
        newStorySection = findViewById(R.id.newStorySection)
        genreEpic = findViewById(R.id.genreEpic)
        genreMystery = findViewById(R.id.genreMystery)
        genreComedy = findViewById(R.id.genreComedy)
        genreHorror = findViewById(R.id.genreHorror)
        etStoryContent = findViewById(R.id.etStoryContent)
        btnStoryfy = findViewById(R.id.btnStoryfy)
        tvStoriesStatus = findViewById(R.id.tvStoriesStatus)
        storiesListContainer = findViewById(R.id.storiesListContainer)

        val genres = listOf(genreEpic, genreMystery, genreComedy, genreHorror)

        btnCreateStory.setOnClickListener {
            toggleForge(newStorySection.visibility == View.GONE)
        }
        btnCommunity.setOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
        }

        fun selectGenre(selected: LinearLayout) {
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

            selectedGenre = when (selected.id) {
                R.id.genreMystery -> "mystery"
                R.id.genreComedy -> "comedy"
                R.id.genreHorror -> "horror"
                else -> "epic"
            }
        }

        genreEpic.setOnClickListener { selectGenre(genreEpic) }
        genreMystery.setOnClickListener { selectGenre(genreMystery) }
        genreComedy.setOnClickListener { selectGenre(genreComedy) }
        genreHorror.setOnClickListener { selectGenre(genreHorror) }
        btnStoryfy.setOnClickListener { createStory() }
        selectGenre(genreEpic)
        loadStories()
    }

    private fun toggleForge(show: Boolean) {
        newStorySection.visibility = if (show) View.VISIBLE else View.GONE
        btnCreateStory.text = if (show) "✕" else "+ CREATE"
        if (!show) {
            etStoryContent.text?.clear()
        }
    }

    private fun createStory() {
        val content = etStoryContent.text.toString()
        lifecycleScope.launch {
            storyRepository.createStory(
                title = storyTitleFromGenre(selectedGenre),
                content = content
            ).onSuccess {
                Toast.makeText(this@StoryActivity, "Story forged", Toast.LENGTH_SHORT).show()
                toggleForge(false)
                loadStories()
            }.onFailure {
                Toast.makeText(this@StoryActivity, it.message ?: "Failed to create story", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadStories() {
        tvStoriesStatus.visibility = View.VISIBLE
        tvStoriesStatus.text = "Loading stories..."
        storiesListContainer.removeAllViews()

        lifecycleScope.launch {
            val storiesResult = storyRepository.getStories()
            val publicStoriesResult = publicStoryRepository.getPublicStories(limit = 100)

            storiesResult.onFailure {
                tvStoriesStatus.text = it.message ?: "Failed to load stories"
                return@launch
            }

            val publicStoryIds = publicStoriesResult.getOrDefault(emptyList()).map { it.storyId }.toSet()
            renderStories(storiesResult.getOrThrow(), publicStoryIds)
        }
    }

    private fun renderStories(stories: List<Story>, publishedStoryIds: Set<String>) {
        storiesListContainer.removeAllViews()
        if (stories.isEmpty()) {
            tvStoriesStatus.visibility = View.VISIBLE
            tvStoriesStatus.text = "No stories yet. Create your first tale."
            return
        }

        tvStoriesStatus.visibility = View.GONE
        val inflater = LayoutInflater.from(this)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        stories.forEach { story ->
            val itemView = inflater.inflate(R.layout.item_story, storiesListContainer, false)
            val published = story.isPublic || publishedStoryIds.contains(story.id)
            itemView.findViewById<TextView>(R.id.tvStoryIcon).text = storyIcon(selectedGenreFromTitle(story.title))
            itemView.findViewById<TextView>(R.id.tvStoryGenre).text = genreLabel(selectedGenreFromTitle(story.title))
            itemView.findViewById<TextView>(R.id.tvStoryTitle).text = story.title
            itemView.findViewById<TextView>(R.id.tvStoryContent).text =
                "${story.content}\n${formatter.format(Date(story.updatedAt))}"
            itemView.findViewById<TextView>(R.id.tvPublishBadge).visibility =
                if (published) View.VISIBLE else View.GONE

            val btnPublish = itemView.findViewById<TextView>(R.id.btnPublishStory)
            btnPublish.text = if (published) "UNPUBLISH" else "PUBLISH"
            btnPublish.setOnClickListener {
                if (published) {
                    unpublishStory(story)
                } else {
                    publishStory(story)
                }
            }

            itemView.findViewById<TextView>(R.id.btnDeleteStory).setOnClickListener {
                deleteStory(story, published)
            }

            storiesListContainer.addView(itemView)
        }
    }

    private fun publishStory(story: Story) {
        lifecycleScope.launch {
            val currentUserId = userRepository.getCurrentUserId()
            if (currentUserId == null) {
                Toast.makeText(this@StoryActivity, "No logged in user", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val currentUser = userRepository.getUserProfile(currentUserId).getOrElse {
                Toast.makeText(this@StoryActivity, it.message ?: "Failed to load user profile", Toast.LENGTH_SHORT).show()
                return@launch
            }

            publicStoryRepository.publishStory(
                story = story,
                authorName = currentUser.name,
                authorAvatarUrl = currentUser.avatarUrl,
                coverImageUrl = story.coverImageUrl
            ).onSuccess { publicStory ->
                storyRepository.updateStory(
                    story.copy(
                        isPublic = true,
                        sharedAt = publicStory.sharedAt,
                        coverImageUrl = publicStory.coverImageUrl
                    )
                ).onFailure {
                    Toast.makeText(
                        this@StoryActivity,
                        it.message ?: "Story published but metadata sync failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Toast.makeText(this@StoryActivity, "Story published", Toast.LENGTH_SHORT).show()
                loadStories()
            }.onFailure {
                Toast.makeText(this@StoryActivity, it.message ?: "Failed to publish story", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unpublishStory(story: Story) {
        lifecycleScope.launch {
            publicStoryRepository.unpublishStory(story.id)
                .onSuccess {
                    storyRepository.updateStory(
                        story.copy(
                            isPublic = false,
                            sharedAt = 0L
                        )
                    ).onFailure {
                        Toast.makeText(
                            this@StoryActivity,
                            it.message ?: "Story unpublished but metadata sync failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Toast.makeText(this@StoryActivity, "Story unpublished", Toast.LENGTH_SHORT).show()
                    loadStories()
                }
                .onFailure {
                    Toast.makeText(this@StoryActivity, it.message ?: "Failed to unpublish story", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteStory(story: Story, published: Boolean) {
        lifecycleScope.launch {
            if (published) {
                publicStoryRepository.unpublishStory(story.id)
                    .onFailure {
                        Toast.makeText(
                            this@StoryActivity,
                            it.message ?: "Failed to unpublish story before delete",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
            }
            storyRepository.deleteStory(story.id)
                .onSuccess {
                    Toast.makeText(this@StoryActivity, "Story deleted", Toast.LENGTH_SHORT).show()
                    loadStories()
                }
                .onFailure {
                    Toast.makeText(this@StoryActivity, it.message ?: "Failed to delete story", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun storyTitleFromGenre(genre: String): String {
        return when (genre) {
            "mystery" -> "Mystery Log"
            "comedy" -> "Comedy Chronicle"
            "horror" -> "Horror Report"
            else -> "Epic Tale"
        }
    }

    private fun selectedGenreFromTitle(title: String): String {
        return when {
            title.contains("Mystery", ignoreCase = true) -> "mystery"
            title.contains("Comedy", ignoreCase = true) -> "comedy"
            title.contains("Horror", ignoreCase = true) -> "horror"
            else -> "epic"
        }
    }

    private fun storyIcon(genre: String): String {
        return when (genre) {
            "mystery" -> "🕵️‍♂️"
            "comedy" -> "🤡"
            "horror" -> "👻"
            else -> "🧙‍♂️"
        }
    }

    private fun genreLabel(genre: String): String {
        return when (genre) {
            "mystery" -> "🕵️ MYSTERY"
            "comedy" -> "😂 COMEDY"
            "horror" -> "👻 HORROR"
            else -> "⚔️ EPIC"
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navQuest).setOnClickListener {
            startActivity(Intent(this, QuestActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navTime).setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
