package com.example.mobileapp

import android.os.Bundle
import android.widget.EditText
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

class EditProfileActivity : BaseActivity() {

    private val viewModel: ProfileViewModel by viewModels { ViewModelFactory() }
    private var selectedAvatarResId: Int = R.drawable.ic_avatar_1

    private var ivCurrentAvatar: ImageView? = null
    private var etUsername: EditText? = null
    private var etUserTitle: EditText? = null
    private var etBio: EditText? = null
    private var tvLevel: TextView? = null
    private var tvExpValue: TextView? = null
    private var expProgressBar: ProgressBar? = null
    
    private val avatarViews = mutableListOf<ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        initViews()
        setupUI()
        observeViewModel()
    }

    private fun initViews() {
        ivCurrentAvatar = findViewById(R.id.ivCurrentAvatar)
        etUsername = findViewById(R.id.etUsername)
        etUserTitle = findViewById(R.id.etUserTitle)
        etBio = findViewById(R.id.etBio)
        tvLevel = findViewById(R.id.tvLevel)
        tvExpValue = findViewById(R.id.tvExpValue)
        expProgressBar = findViewById(R.id.expProgressBar)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }

    private fun setupUI() {
        val btnSaveProfile = findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnBack = findViewById<TextView>(R.id.btnBack)

        setupAvatarSelection()

        btnSaveProfile?.setOnClickListener {
            val username = etUsername?.text?.toString() ?: ""
            val title = etUserTitle?.text?.toString() ?: ""
            val bio = etBio?.text?.toString() ?: ""

            if (username.isBlank()) {
                Toast.makeText(this, getString(R.string.error_username_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val avatarUrl = try {
                resources.getResourceEntryName(selectedAvatarResId)
            } catch (e: Exception) {
                "ic_avatar_1"
            }
            viewModel.updateProfile(username, avatarUrl, title, bio)
        }

        btnBack?.setOnClickListener {
            finish()
        }
    }

    private fun setupAvatarSelection() {
        val avatarIds = listOf(
            R.id.avatar1 to R.drawable.ic_avatar_1,
            R.id.avatar2 to R.drawable.ic_avatar_2,
            R.id.avatar3 to R.drawable.ic_avatar_3,
            R.id.avatar4 to R.drawable.ic_avatar_4,
            R.id.avatar5 to R.drawable.ic_avatar_5,
            R.id.avatar6 to R.drawable.ic_avatar_6
        )

        avatarViews.clear()
        avatarIds.forEach { (viewId, resId) ->
            findViewById<ImageView>(viewId)?.let { view ->
                avatarViews.add(view)
                view.setOnClickListener {
                    selectAvatar(resId, view)
                }
            }
        }
    }

    private fun selectAvatar(resId: Int, selectedView: ImageView) {
        selectedAvatarResId = resId
        ivCurrentAvatar?.setImageResource(resId)

        // Update UI selection state
        avatarViews.forEach { 
            it.setBackgroundResource(R.drawable.bg_button_unselected)
        }
        selectedView.setBackgroundResource(R.drawable.bg_button_selected)
    }

    private var isProfileLoaded = false

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userProfile.collect { user ->
                    user?.let {
                        if (!isProfileLoaded) {
                            etUsername?.setText(it.name)
                            etUserTitle?.setText(it.title)
                            etBio?.setText(it.bio)
                            isProfileLoaded = true
                        }
                        
                        // Set current avatar if valid
                        if (it.avatarUrl.isNotBlank()) {
                            val resId = resources.getIdentifier(it.avatarUrl, "drawable", packageName)
                            if (resId != 0) {
                                selectedAvatarResId = resId
                                ivCurrentAvatar?.setImageResource(resId)
                                updateAvatarSelectionUI(resId)
                            }
                        }

                        tvLevel?.text = getString(R.string.level_format, it.level)
                        tvExpValue?.text = getString(R.string.exp_format, it.exp, it.level * 100)
                        expProgressBar?.progress = (it.exp % 100)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateResult.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            Toast.makeText(this@EditProfileActivity, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                            viewModel.clearUpdateResult()
                            finish()
                        } else {
                            Toast.makeText(this@EditProfileActivity, getString(R.string.profile_save_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Toast.makeText(this@EditProfileActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun updateAvatarSelectionUI(selectedResId: Int) {
        val avatarIds = listOf(
            R.id.avatar1 to R.drawable.ic_avatar_1,
            R.id.avatar2 to R.drawable.ic_avatar_2,
            R.id.avatar3 to R.drawable.ic_avatar_3,
            R.id.avatar4 to R.drawable.ic_avatar_4,
            R.id.avatar5 to R.drawable.ic_avatar_5,
            R.id.avatar6 to R.drawable.ic_avatar_6
        )

        avatarIds.forEach { (viewId, resId) ->
            findViewById<ImageView>(viewId)?.let { view ->
                if (resId == selectedResId) {
                    view.setBackgroundResource(R.drawable.bg_button_selected)
                } else {
                    view.setBackgroundResource(R.drawable.bg_button_unselected)
                }
            }
        }
    }
}
