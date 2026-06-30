package com.example.mobileapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

/**
 * Edit Profile screen.
 * Reached from ProfileActivity / SettingsActivity via btnEditProfile.
 */
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
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnSaveProfile).setOnClickListener {
            Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
