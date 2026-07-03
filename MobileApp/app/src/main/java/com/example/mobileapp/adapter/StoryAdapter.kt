package com.example.mobileapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.util.AnimationUtils.slideUp

class StoryAdapter(
    private val onClick: (StoryItem) -> Unit,
    private val onDelete: (StoryItem) -> Unit
) : ListAdapter<StoryItem, StoryAdapter.StoryViewHolder>(StoryDiffCallback()) {

    private var lastAnimatedPosition = -1

    fun updateStories(newStories: List<StoryItem>) {
        submitList(newStories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        holder.bind(getItem(position), onClick, onDelete)
        if (currentPosition > lastAnimatedPosition) {
            holder.itemView.slideUp(delay = (currentPosition * 30).coerceAtMost(300).toLong())
            lastAnimatedPosition = currentPosition
        }
    }

    class StoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvStoryEmoji: TextView = view.findViewById(R.id.tvStoryEmoji)
        private val tvStoryGenre: TextView = view.findViewById(R.id.tvStoryGenre)
        private val tvStoryTitle: TextView = view.findViewById(R.id.tvStoryTitle)
        private val tvStoryPreview: TextView = view.findViewById(R.id.tvStoryPreview)
        private val tvStoryDate: TextView = view.findViewById(R.id.tvStoryDate)
        private val btnDeleteStory: TextView = view.findViewById(R.id.btnDeleteStory)

        fun bind(story: StoryItem, onClick: (StoryItem) -> Unit, onDelete: (StoryItem) -> Unit) {
            tvStoryGenre.text = story.genre.uppercase()
            tvStoryTitle.text = story.title
            tvStoryPreview.text = story.content
            tvStoryDate.text = story.date

            tvStoryEmoji.text = when(story.genre.uppercase()) {
                "EPIC" -> "⚔️"
                "MYSTERY" -> "🔍"
                "COMEDY" -> "🎭"
                "HORROR" -> "👻"
                else -> "📖"
            }

            itemView.setOnClickListener { onClick(story) }
            btnDeleteStory.setOnClickListener { onDelete(story) }
        }
    }

    class StoryDiffCallback : DiffUtil.ItemCallback<StoryItem>() {
        override fun areItemsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StoryItem, newItem: StoryItem): Boolean = oldItem == newItem
    }
}
