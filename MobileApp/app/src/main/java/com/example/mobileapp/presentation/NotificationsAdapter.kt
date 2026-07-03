package com.example.mobileapp.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobileapp.R
import com.example.mobileapp.domain.model.AppNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsAdapter(
    private val onNotificationClick: (AppNotification) -> Unit,
) : ListAdapter<AppNotification, NotificationsAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view, onNotificationClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        view: View,
        private val onClick: (AppNotification) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvNotifTitle)
        private val tvMessage: TextView = view.findViewById(R.id.tvNotifMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvNotifTime)
        private val tvIcon: TextView = view.findViewById(R.id.tvNotifIcon)
        private val indicator: View = view.findViewById(R.id.viewUnreadIndicator)

        fun bind(notification: AppNotification) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message
            
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = sdf.format(Date(notification.timestamp))

            tvIcon.text = when(notification.type) {
                "calendar" -> "⚔️"
                "timer" -> "⏰"
                else -> "🔔"
            }
            indicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE
            
            itemView.setOnClickListener { onClick(notification) }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<AppNotification>() {
        override fun areItemsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean = oldItem == newItem
    }
}
