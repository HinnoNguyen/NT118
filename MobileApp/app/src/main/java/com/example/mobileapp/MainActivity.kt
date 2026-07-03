package com.example.mobileapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.MainViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.example.mobileapp.util.AnimationUtils.setBounceClick
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels { ViewModelFactory() }
    
    private lateinit var tvUserName: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvExpValue: TextView
    private lateinit var expProgressBar: ProgressBar
    private lateinit var ivUserAvatar: ImageView
    
    private lateinit var tvQuestsDone: TextView
    private lateinit var tvNotesWritten: TextView
    private lateinit var tvFocusTime: TextView
    private lateinit var tvStreak: TextView

    private lateinit var tvDailyQuestTasks: TextView
    private lateinit var tvDailyQuestFocus: TextView
    private lateinit var tvDailyQuestNote: TextView
    private lateinit var tvMainMessage: TextView
    private lateinit var tvGameRewardStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupUI()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }


    private fun setupUI() {
        tvUserName = findViewById(R.id.tvUserName)
        tvLevel = findViewById(R.id.tvLevel)
        tvExpValue = findViewById(R.id.tvExpValue)
        expProgressBar = findViewById(R.id.expProgressBar)
        ivUserAvatar = findViewById(R.id.ivUserAvatar)
        
        tvQuestsDone = findViewById(R.id.tvQuestsDone)
        tvNotesWritten = findViewById(R.id.tvNotesWritten)
        tvFocusTime = findViewById(R.id.tvFocusTime)
        tvStreak = findViewById(R.id.tvStreak)

        tvDailyQuestTasks = findViewById(R.id.tvDailyQuestTasks)
        tvDailyQuestFocus = findViewById(R.id.tvDailyQuestFocus)
        tvDailyQuestNote = findViewById(R.id.tvDailyQuestNote)
        tvMainMessage = findViewById(R.id.tvMainMessage)
        tvGameRewardStatus = findViewById(R.id.tvGameRewardStatus)
        
        val userInfoCard = findViewById<LinearLayout>(R.id.userInfoCard)
        userInfoCard.setBounceClick()
        userInfoCard.setOnClickListener {
            navigateTo(ProfileActivity::class.java)
        }

        val btnPlayGame = findViewById<LinearLayout>(R.id.btnPlayGame)
        btnPlayGame.setBounceClick()
        btnPlayGame.setOnClickListener {
            navigateTo(SnakeGameActivity::class.java)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userProfile.collect { user ->
                        user?.let {
                            tvUserName.text = it.name
                            tvLevel.text = getString(R.string.level_format, it.level)
                            tvExpValue.text = getString(R.string.exp_format, it.exp, it.level * 100)
                            
                            expProgressBar.setProgress(it.exp % 100, true)
                            
                            tvQuestsDone.text = it.completedTaskCount.toString()
                            val hours = it.totalFocusMinutes / 60
                            val minutes = it.totalFocusMinutes % 60
                            tvFocusTime.text = getString(R.string.focus_time_format, hours, minutes)
                            tvStreak.text = it.currentStreak.toString()

                            // Load Avatar
                            if (it.avatarUrl.isNotBlank()) {
                                val resId = resources.getIdentifier(it.avatarUrl, "drawable", packageName)
                                if (resId != 0) {
                                    ivUserAvatar.setImageResource(resId)
                                }
                            }
                            
                            // Daily Quest Focus Progress
                            val focusProgress = minOf(it.todayFocusMinutes, 30)
                            val focusCheck = if (focusProgress >= 30) "■" else "□"
                            tvDailyQuestFocus.text = getString(R.string.daily_quest_focus, focusCheck, focusProgress)

                            // Mini Game Reward Status
                            val isNewDay = !isSameDay(it.lastMiniGameRewardAt, System.currentTimeMillis())
                            val currentRewardCount = if (isNewDay) 0 else it.miniGameRewardCount
                            val remainingRewards = maxOf(0, 3 - currentRewardCount)
                            tvGameRewardStatus.text = getString(R.string.game_bonus_exp, remainingRewards)
                        }
                    }
                }
                launch {
                    viewModel.notesCount.collect { count ->
                        tvNotesWritten.text = count.toString()
                        val noteDone = count > 0 
                        val noteCheck = if (noteDone) "■" else "□"
                        tvDailyQuestNote.text = getString(R.string.daily_quest_note, noteCheck, minOf(count, 1))
                    }
                }
                launch {
                    viewModel.todayTasksCompleted.collect { count ->
                        val taskProgress = minOf(count, 3)
                        val taskCheck = if (taskProgress >= 3) "■" else "□"
                        tvDailyQuestTasks.text = getString(R.string.daily_quest_task, taskCheck, taskProgress)
                    }
                }
                launch {
                    viewModel.remainingQuests.collect { count ->
                        val newMessage = if (count > 0) {
                            getString(R.string.quests_remaining, count)
                        } else {
                            getString(R.string.quests_all_completed)
                        }
                        if (tvMainMessage.text != newMessage) {
                            tvMainMessage.animate().cancel()
                            tvMainMessage.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction {
                                    tvMainMessage.text = newMessage
                                    tvMainMessage.animate()
                                        .alpha(1f)
                                        .setDuration(200)
                                        .start()
                                }
                                .start()
                        }
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

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
