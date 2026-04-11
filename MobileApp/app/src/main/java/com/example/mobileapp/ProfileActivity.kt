package com.example.mobileapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mobileapp.presentation.profile.ProfileViewModel

/**
 * Profile screen.
 * Communicates only with [ProfileViewModel]; never imports repositories or use cases.
 */
class ProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
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
    }
}
