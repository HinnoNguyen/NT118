package com.example.mobileapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.domain.model.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter : ListAdapter<Comment, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvAuthor = view.findViewById<TextView>(R.id.tvCommentAuthor)
        private val tvContent = view.findViewById<TextView>(R.id.tvCommentContent)
        private val tvDate = view.findViewById<TextView>(R.id.tvCommentDate)
        private val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        fun bind(comment: Comment) {
            tvAuthor.text = comment.userName
            tvContent.text = comment.content
            tvDate.text = formatter.format(Date(comment.createdAt))
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean = oldItem == newItem
    }
}
