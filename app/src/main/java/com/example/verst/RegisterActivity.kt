package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.verst.databinding.ActivityRegisterBinding
import com.example.verst.network.ApiClient
import com.example.verst.network.RegisterRemote
import com.example.verst.network.RegisterReceiveRemote
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    companion object {
        private const val SHARED_PREFS_NAME = "tour_app_prefs"
        private const val TOKEN_KEY = "auth_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Стрелка назад
        binding.backArrow.setOnClickListener {
            finish()
        }

        // Кнопка регистрации
        binding.registerButton.setOnClickListener {
            val login = binding.loginInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateRegisterFields(login, email, password)) {
                registerUser(login, email, password)
            }
        }
    }

    private fun validateRegisterFields(login: String, email: String, password: String): Boolean {
        if (login.isEmpty()) {
            binding.loginInput.error = "Login cannot be empty"
            return false
        }
        if (email.isEmpty()) {
            binding.emailInput.error = "Email cannot be empty"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Invalid email format"
            return false
        }
        if (password.isEmpty()) {
            binding.passwordInput.error = "Password cannot be empty"
            return false
        }
        return true
    }

    private fun registerUser(login: String, email: String, password: String) {
        binding.registerButton.isEnabled = false

        val request = RegisterRemote(login, email, password)
        ApiClient.apiService.register(request).enqueue(object : Callback<RegisterReceiveRemote> {
            override fun onResponse(call: Call<RegisterReceiveRemote>, response: Response<RegisterReceiveRemote>) {
                binding.registerButton.isEnabled = true

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        val sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
                        sharedPrefs.edit().putString(TOKEN_KEY, token).apply()
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Invalid response from server", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Invalid email format"
                        409 -> "User already exists"
                        else -> "Registration failed: ${response.message()}"
                    }
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RegisterReceiveRemote>, t: Throwable) {
                binding.registerButton.isEnabled = true
                Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}