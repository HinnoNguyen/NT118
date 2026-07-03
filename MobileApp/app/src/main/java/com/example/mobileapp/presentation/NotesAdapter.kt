package com.example.mobileapp.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.domain.model.Note
import com.example.mobileapp.util.AnimationUtils.slideUp
import java.text.SimpleDateFormat
import java.util.Locale

class NotesAdapter(
    private val onDeleteClick: (String) -> Unit,
    private val onShareClick: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view, onDeleteClick, onShareClick)
    }

    private var lastAnimatedPosition = -1

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentPosition = holder.adapterPosition
        holder.bind(getItem(position))
        if (currentPosition > lastAnimatedPosition) {
            holder.itemView.slideUp(delay = (currentPosition * 30).coerceAtMost(300).toLong())
            lastAnimatedPosition = currentPosition
        }
    }

    class NoteViewHolder(
        view: View,
        private val onDeleteClick: (String) -> Unit,
        private val onShareClick: (Note) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val tvNoteTitle: TextView = view.findViewById(R.id.tvNoteTitle)
        private val tvNoteContent: TextView = view.findViewById(R.id.tvNoteContent)
        private val tvNoteIcon: TextView = view.findViewById(R.id.tvNoteIcon)
        private val tvReminderTime: TextView = view.findViewById(R.id.tvReminderTime)
        private val btnDeleteNote: TextView = view.findViewById(R.id.btnDeleteNote)
        private val btnShareNote: TextView = view.findViewById(R.id.btnShareNote)

        fun bind(note: Note) {
            tvNoteTitle.text = note.title
            tvNoteContent.text = note.content
            
            tvNoteIcon.text = when(note.type) {
                "reminder" -> "🔔"
                "flashcard" -> "🃏"
                else -> "📑"
            }

            if (note.reminderTime != null && note.type == "reminder") {
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                tvReminderTime.text = sdf.format(note.reminderTime)
                tvReminderTime.visibility = View.VISIBLE
            } else {
                tvReminderTime.visibility = View.GONE
            }

            btnDeleteNote.setOnClickListener {
                onDeleteClick(note.id)
            }

            btnShareNote.setOnClickListener {
                onShareClick(note)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem
    }
}
