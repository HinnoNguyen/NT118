package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        setupEdgeToEdge()
        setupUI()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupUI() {
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etUserTitle = findViewById<EditText>(R.id.etUserTitle)
        val etBio = findViewById<EditText>(R.id.etBio)
        val btnSaveProfile = findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnBack = findViewById<TextView>(R.id.btnBack)

        btnSaveProfile.setOnClickListener {
            val username = etUsername.text.toString()
            val userTitle = etUserTitle.text.toString()
            val bio = etBio.text.toString()

            if (username.isBlank()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to shared preferences as a mock for now
            val sharedPreferences = getSharedPreferences("user_profile_prefs", MODE_PRIVATE)
            sharedPreferences.edit().apply {
                putString("username", username)
                putString("user_title", userTitle)
                putString("bio", bio)
                apply()
            }

            Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
