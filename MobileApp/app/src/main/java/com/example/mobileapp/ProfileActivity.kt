package com.example.mobileapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.ProfileViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class ProfileActivity : BaseActivity() {

    private val viewModel: ProfileViewModel by viewModels { ViewModelFactory() }

    private var tvUserName: TextView? = null
    private var tvUserTitle: TextView? = null
    private var tvLevel: TextView? = null
    private var tvExpValue: TextView? = null
    private var expProgressBar: ProgressBar? = null
    private var tvBio: TextView? = null
    private var ivProfileAvatar: ImageView? = null
    
    private var tvQuestsDone: TextView? = null
    private var tvNotesWritten: TextView? = null
    private var tvFocusTime: TextView? = null
    private var tvStoriesCount: TextView? = null
    private var tvStreak: TextView? = null
    private var tvBestStreak: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initViews()
        setupUI()
        observeViewModel()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserTitle = findViewById(R.id.tvUserTitle)
        tvLevel = findViewById(R.id.tvLevel)
        tvExpValue = findViewById(R.id.tvExpValue)
        expProgressBar = findViewById(R.id.expProgressBar)
        tvBio = findViewById(R.id.tvBio)
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar)
        
        tvQuestsDone = findViewById(R.id.tvQuestsDone)
        tvNotesWritten = findViewById(R.id.tvNotesWritten)
        tvFocusTime = findViewById(R.id.tvFocusTime)
        tvStoriesCount = findViewById(R.id.tvStoriesCount)
        tvStreak = findViewById(R.id.tvStreak)
        tvBestStreak = findViewById(R.id.tvBestStreak)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }

    private fun setupUI() {
        findViewById<TextView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
        findViewById<MaterialButton>(R.id.btnEditProfile)?.setOnClickListener {
            navigateTo(EditProfileActivity::class.java)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { showLoading(it) }
                }
                launch {
                    viewModel.userProfile.collect { user ->
                    user?.let {
                        tvUserName?.text = it.name
                        tvUserTitle?.text = if (it.title.isNotBlank()) it.title else "Pixel Knight"
                        tvLevel?.text = "LV.${it.level}"
                        tvExpValue?.text = "${it.exp}/${it.level * 100}"
                        expProgressBar?.setProgress(it.exp % 100, true)
                        tvBio?.text = if (it.bio.isNotBlank()) it.bio else "Adventurer of productivity realms"

                        if (it.avatarUrl.isNotBlank()) {
                            val resId = resources.getIdentifier(it.avatarUrl, "drawable", packageName)
                            if (resId != 0) {
                                ivProfileAvatar?.setImageResource(resId)
                            }
                        }
                        
                        tvQuestsDone?.text = it.completedTaskCount.toString()
                        val hours = it.totalFocusMinutes / 60
                        val minutes = it.totalFocusMinutes % 60
                        tvFocusTime?.text = "${hours}h ${minutes}m"
                        tvNotesWritten?.text = "8" // Mock
                        tvStoriesCount?.text = "3" // Mock
                        tvStreak?.text = it.currentStreak.toString()
                        tvBestStreak?.text = it.bestStreak.toString()
                    }
                }
            }
        }
    }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Toast.makeText(this@ProfileActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}
