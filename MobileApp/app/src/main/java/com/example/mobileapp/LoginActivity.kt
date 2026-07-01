package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.LoginViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        setupEdgeToEdge()
        setupUI()
        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val btnThemeToggle = findViewById<Button>(R.id.btnThemeToggle)

        btnThemeToggle.setOnClickListener {
            val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", true)
            val newDarkMode = !isDarkMode
            val currentMode = if (newDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            
            sharedPreferences.edit().putBoolean("is_dark_mode", newDarkMode).apply()
            AppCompatDelegate.setDefaultNightMode(currentMode)
            
            // Re-create the activity to apply theme change immediately and clearly
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun setupUI() {
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnStartGame)
        val btnRegisterTab = findViewById<TextView>(R.id.btnRegisterTab)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            
            if (email.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, pass)
            }
        }

        btnRegisterTab.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
            finish()
        }

        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.stay)
        }

        btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    handleLoginState(state, btnLogin)
                }
            }
        }
    }

    private fun handleLoginState(state: LoginViewModel.LoginState, btnLogin: Button) {
        when (state) {
            is LoginViewModel.LoginState.Loading -> {
                btnLogin.isEnabled = false
                btnLogin.text = "LOADING..."
            }
            is LoginViewModel.LoginState.Success -> {
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                Toast.makeText(this, "Welcome Hero!", Toast.LENGTH_SHORT).show()
                BaseActivity.resetNavigationState()
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
            is LoginViewModel.LoginState.Error -> {
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
}
