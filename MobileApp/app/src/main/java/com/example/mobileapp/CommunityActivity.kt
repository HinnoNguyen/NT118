package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobileapp.data.repository.PublicStoryRepositoryImpl
import com.example.mobileapp.domain.model.PublicStory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityActivity : AppCompatActivity() {
    private val publicStoryRepository = PublicStoryRepositoryImpl()

    private lateinit var btnBackToStories: TextView
    private lateinit var tvCommunityStatus: TextView
    private lateinit var publicStoriesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        setupUi()
        setupNavigation()
        loadPublicStories()
    }

    private fun setupUi() {
        btnBackToStories = findViewById(R.id.btnBackToStories)
        tvCommunityStatus = findViewById(R.id.tvCommunityStatus)
        publicStoriesContainer = findViewById(R.id.publicStoriesContainer)

        btnBackToStories.setOnClickListener {
            startActivity(Intent(this, StoryActivity::class.java))
            finish()
        }
    }

    private fun loadPublicStories() {
        tvCommunityStatus.visibility = View.VISIBLE
        tvCommunityStatus.text = "Loading public stories..."
        publicStoriesContainer.removeAllViews()

        lifecycleScope.launch {
            publicStoryRepository.getPublicStories(limit = 100)
                .onSuccess { renderPublicStories(it) }
                .onFailure {
                    tvCommunityStatus.text = it.message ?: "Failed to load community feed"
                    tvCommunityStatus.visibility = View.VISIBLE
                }
        }
    }

    private fun renderPublicStories(stories: List<PublicStory>) {
        publicStoriesContainer.removeAllViews()
        if (stories.isEmpty()) {
            tvCommunityStatus.text = "No public stories yet."
            tvCommunityStatus.visibility = View.VISIBLE
            return
        }

        tvCommunityStatus.visibility = View.GONE
        val inflater = LayoutInflater.from(this)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        stories.forEach { story ->
            val itemView = inflater.inflate(R.layout.item_public_story, publicStoriesContainer, false)
            itemView.findViewById<TextView>(R.id.tvPublicStoryAuthor).text = story.authorName
            itemView.findViewById<TextView>(R.id.tvPublicStoryTitle).text = story.title
            itemView.findViewById<TextView>(R.id.tvPublicStoryPreview).text =
                story.contentPreview.ifBlank { "(No preview available)" }
            itemView.findViewById<TextView>(R.id.tvPublicStoryMeta).text =
                "${formatter.format(Date(story.sharedAt))} • ${story.visibility.uppercase()}"
            itemView.findViewById<TextView>(R.id.tvPublicStoryStats).text =
                "❤ ${story.likeCount}   💬 ${story.commentCount}"

            publicStoriesContainer.addView(itemView)
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
        findViewById<LinearLayout>(R.id.navStory).setOnClickListener {
            startActivity(Intent(this, StoryActivity::class.java))
            finish()
        }
        findViewById<LinearLayout>(R.id.navSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
