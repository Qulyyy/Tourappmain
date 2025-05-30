package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.verst.databinding.ActivityLoginBinding
import com.example.verst.network.ApiClient
import com.example.verst.network.LoginRemote
import com.example.verst.network.LoginReceiveRemote
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    companion object {
        private const val SHARED_PREFS_NAME = "tour_app_prefs"
        private const val TOKEN_KEY = "auth_token"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверка авторизации
        val sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val token = sharedPrefs.getString(TOKEN_KEY, null)
        if (token != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Стрелка назад
        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Кнопка логина
        binding.loginButton.setOnClickListener {
            val login = binding.emailInput.text.toString().trim() // Логин в email_input
            val password = binding.passwordInput.text.toString().trim()

            if (validateLoginFields(login, password)) {
                loginUser(login, password)
            }
        }

        // Переход к регистрации
        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Забыл пароль
        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked (not implemented)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateLoginFields(login: String, password: String): Boolean {
        if (login.isEmpty()) {
            binding.emailInput.error = "Login cannot be empty"
            return false
        }
        if (password.isEmpty()) {
            binding.passwordInput.error = "Password cannot be empty"
            return false
        }
        return true
    }

    private fun loginUser(login: String, password: String) {
        binding.loginProgress.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        val request = LoginRemote(login, password)
        ApiClient.apiService.login(request).enqueue(object : Callback<LoginReceiveRemote> {
            override fun onResponse(call: Call<LoginReceiveRemote>, response: Response<LoginReceiveRemote>) {
                binding.loginProgress.visibility = View.GONE
                binding.loginButton.isEnabled = true

                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        val sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
                        sharedPrefs.edit().putString(TOKEN_KEY, token).apply()
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid response from server", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid login or password"
                        else -> "Login failed: ${response.message()}"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginReceiveRemote>, t: Throwable) {
                binding.loginProgress.visibility = View.GONE
                binding.loginButton.isEnabled = true
                Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}