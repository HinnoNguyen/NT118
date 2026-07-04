package com.example.mobileapp

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobileapp.util.ThemeUtils
import com.example.mobileapp.util.LocaleHelper
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
        setupLanguageSelection()
    }

    private fun setupLanguageSelection() {
        val btnLanguage = findViewById<LinearLayout>(R.id.btnLanguage)
        val tvCurrentLanguage = findViewById<TextView>(R.id.tvCurrentLanguage)
        
        val currentLang = LocaleHelper.getLanguage(this)
        tvCurrentLanguage.text = if (currentLang == "vi") "Tiếng Việt" else "English"

        btnLanguage.setOnClickListener {
            val languages = arrayOf("English", "Tiếng Việt")
            val langCodes = arrayOf("en", "vi")
            
            AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setItems(languages) { _, which: Int ->
                    val selectedLang = langCodes[which]
                    if (selectedLang != currentLang) {
                        LocaleHelper.setLocale(this, selectedLang)
                        // Refresh the activity to apply language change
                        val intent = intent
                        finish()
                        startActivity(intent)
                    }
                }
                .show()
        }
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
        
        switchDarkMode.setOnCheckedChangeListener(null)
        switchDarkMode.isChecked = isDarkMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (sharedPreferences.getBoolean("is_dark_mode", !isChecked) != isChecked) {
                val root = findViewById<ViewGroup>(R.id.main)
                if (!ThemeUtils.toggleTheme(this, root, switchDarkMode, isChecked)) {
                    // Rate limited: revert switch state
                    switchDarkMode.setOnCheckedChangeListener(null)
                    switchDarkMode.isChecked = !isChecked
                    setupDarkModeSwitch() // Re-attach listener
                    android.widget.Toast.makeText(this, "Please wait 5s before toggling again", android.widget.Toast.LENGTH_SHORT).show()
                }
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
