package com.example.mobileapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mobileapp.data.repository.UserRepositoryImpl
import com.example.mobileapp.domain.usecase.RegisterUseCase
import com.example.mobileapp.presentation.RegisterViewModel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        setupEdgeToEdge()
        setupViewModel()
        setupUI()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupViewModel() {
        val repository = UserRepositoryImpl()
        val registerUseCase = RegisterUseCase(repository)
        viewModel = RegisterViewModel(registerUseCase)
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
                startActivity(Intent(this, MainActivity::class.java))
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
