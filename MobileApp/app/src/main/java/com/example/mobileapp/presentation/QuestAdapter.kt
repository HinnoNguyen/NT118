package com.example.mobileapp.presentation

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.domain.model.Task
import com.example.mobileapp.util.AnimationUtils.slideUp

class QuestAdapter(
    private val onToggleTask: (String, Boolean) -> Unit
) : ListAdapter<Task, QuestAdapter.QuestViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view, onToggleTask)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.slideUp(delay = (position * 50).toLong())
    }

    class QuestViewHolder(
        view: View,
        private val onToggleTask: (String, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val cbComplete: CheckBox = view.findViewById(R.id.cbComplete)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvExp: TextView = view.findViewById(R.id.tvExp)

        fun bind(task: Task) {
            tvTitle.text = task.title
            cbComplete.setOnCheckedChangeListener(null)
            cbComplete.isChecked = task.completed
            
            tvExp.text = when(task.priority) {
                "high" -> "+50xp"
                "normal" -> "+20xp"
                else -> "+10xp"
            }

            if (task.completed) {
                tvTitle.animate().alpha(0.5f).scaleX(0.98f).scaleY(0.98f).setDuration(200).start()
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            cbComplete.setOnCheckedChangeListener { _, isChecked ->
                onToggleTask(task.id, isChecked)
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}
