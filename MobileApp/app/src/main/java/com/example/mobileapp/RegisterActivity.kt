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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.presentation.auth.RegisterViewModel
import com.example.mobileapp.utils.Resource
import kotlinx.coroutines.launch

/**
 * Auth screen — registration.
 * Communicates only with [RegisterViewModel]; never imports repositories or use cases.
 */
class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels { RegisterViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        setupEdgeToEdge()
        setupUI()
        observeViewModel()
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
        val tvSignInLink = findViewById<TextView>(R.id.tvSignInLink)
        val btnGoogleSignUp = findViewById<Button>(R.id.btnGoogleSignUp)

        btnRegister.setOnClickListener {
            viewModel.register(
                etUsername.text.toString().trim(),
                etEmail.text.toString().trim(),
                etPassword.text.toString(),
                etConfirmPassword.text.toString()
            )
        }

        val navigateToLogin = {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        btnLoginTab.setOnClickListener { navigateToLogin() }
        tvSignInLink.setOnClickListener { navigateToLogin() }

        btnGoogleSignUp.setOnClickListener {
            Toast.makeText(this, "Google Sign Up coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            btnRegister.isEnabled = false
                            btnRegister.text = "LOADING..."
                        }
                        is Resource.Success -> {
                            btnRegister.isEnabled = true
                            btnRegister.text = "REGISTER"
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration Successful! Welcome, ${resource.data.username}",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        is Resource.Error -> {
                            btnRegister.isEnabled = true
                            btnRegister.text = "REGISTER"
                            Toast.makeText(this@RegisterActivity, resource.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> { /* idle */ }
                    }
                }
            }
        }
    }
}
