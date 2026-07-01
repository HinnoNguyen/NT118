package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        setupEdgeToEdge()
        setupNavigation()
        setupLogout()
        setupDarkModeSwitch()
        setupEditProfile()
    }

    private fun setupEditProfile() {
        findViewById<LinearLayout>(R.id.btnEditProfile).setOnClickListener {
            navigateTo(EditProfileActivity::class.java)
        }
    }

    private fun setupDarkModeSwitch() {
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val switchDarkMode = findViewById<CompoundButton>(R.id.switchDarkMode)
        val rowDarkMode = findViewById<LinearLayout>(R.id.rowDarkMode)
        
        if (switchDarkMode == null || rowDarkMode == null) return

        val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", true)
        
        // Disable listener before setting initial state
        switchDarkMode.setOnCheckedChangeListener(null)
        switchDarkMode.isChecked = isDarkMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val currentMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            
            if (sharedPreferences.getBoolean("is_dark_mode", !isChecked) != isChecked) {
                sharedPreferences.edit().putBoolean("is_dark_mode", isChecked).apply()
                AppCompatDelegate.setDefaultNightMode(currentMode)

                // Re-create the activity stack to apply theme change immediately and clearly
                val intent = Intent(this, SettingsActivity::class.java)
                finish()
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        
        rowDarkMode.setOnClickListener {
            switchDarkMode.isChecked = !switchDarkMode.isChecked
        }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupLogout() {
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            // Sign out from Firebase Auth
            FirebaseAuth.getInstance().signOut()
            resetNavigationState()
            
            // Chuyển về màn hình Login và xóa hết lịch sử các màn hình trước đó
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupNavigation() {
        // Handled by BaseActivity
    }
}
