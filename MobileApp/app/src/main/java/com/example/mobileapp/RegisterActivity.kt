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
import com.example.mobileapp.presentation.RegisterViewModel
import com.example.mobileapp.presentation.ViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

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
            val intent = Intent(this, RegisterActivity::class.java)
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
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLoginTab = findViewById<TextView>(R.id.btnLoginTab)
        val btnGoogleSignUp = findViewById<Button>(R.id.btnGoogleSignUp)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            val confirmPass = etConfirmPassword.text.toString()

            viewModel.register(username, email, pass, confirmPass)
        }

        val navigateToLogin = {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.stay)
            finish()
        }

        btnLoginTab.setOnClickListener { navigateToLogin() }
        
        btnGoogleSignUp.setOnClickListener {
            Toast.makeText(this, "Google Sign Up coming soon!", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    handleRegisterState(state, btnRegister)
                }
            }
        }
    }

    private fun handleRegisterState(state: RegisterViewModel.RegisterState, btnRegister: Button) {
        when (state) {
            is RegisterViewModel.RegisterState.Loading -> {
                btnRegister.isEnabled = false
                btnRegister.text = "LOADING..."
            }
            is RegisterViewModel.RegisterState.Success -> {
                btnRegister.isEnabled = true
                btnRegister.text = "REGISTER"
                Toast.makeText(this, "Registration Successful! Welcome, ${state.user.name}", Toast.LENGTH_SHORT).show()
                BaseActivity.resetNavigationState()
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
            is RegisterViewModel.RegisterState.Error -> {
                btnRegister.isEnabled = true
                btnRegister.text = "REGISTER"
                Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
}
