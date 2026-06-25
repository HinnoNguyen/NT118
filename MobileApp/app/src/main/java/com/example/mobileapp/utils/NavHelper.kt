package com.example.mobileapp.utils

import android.app.Activity
import android.content.Intent
import android.widget.LinearLayout
import com.example.mobileapp.MainActivity
import com.example.mobileapp.NotesActivity
import com.example.mobileapp.QuestActivity
import com.example.mobileapp.R
import com.example.mobileapp.SettingsActivity
import com.example.mobileapp.StoryActivity
import com.example.mobileapp.TimerActivity

/**
 * Centralised bottom-navigation helper.
 *
 * Usage – call once from each Activity's onCreate (after setContentView):
 *
 *     NavHelper.setup(this, NavHelper.Screen.HOME)
 *
 * The [current] parameter prevents re-launching the activity that is already
 * on screen, which avoids a redundant back-stack entry.
 */
object NavHelper {

    enum class Screen { HOME, QUEST, TIME, NOTES, STORY, SETTINGS }

    fun setup(activity: Activity, current: Screen) {
        val navIds = mapOf(
            R.id.navHome     to Screen.HOME,
            R.id.navQuest    to Screen.QUEST,
            R.id.navTime     to Screen.TIME,
            R.id.navNotes    to Screen.NOTES,
            R.id.navStory    to Screen.STORY,
            R.id.navSettings to Screen.SETTINGS
        )

        for ((viewId, screen) in navIds) {
            activity.findViewById<LinearLayout>(viewId)?.setOnClickListener {
                if (screen == current) return@setOnClickListener   // already here
                val intent = Intent(activity, screenToClass(screen))
                // Clear the back stack so the nav bar always feels top-level
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                activity.startActivity(intent)
            }
        }
    }

    private fun screenToClass(screen: Screen): Class<out Activity> = when (screen) {
        Screen.HOME     -> MainActivity::class.java
        Screen.QUEST    -> QuestActivity::class.java
        Screen.TIME     -> TimerActivity::class.java
        Screen.NOTES    -> NotesActivity::class.java
        Screen.STORY    -> StoryActivity::class.java
        Screen.SETTINGS -> SettingsActivity::class.java
    }
}
