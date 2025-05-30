package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.verst.databinding.ActivityCreateTourBinding
import com.example.verst.network.ApiClient
import com.example.verst.network.CreateTourRequest
import com.example.verst.network.UserRemote
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateTourActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTourBinding
    private val sharedPrefs by lazy { getSharedPreferences("tour_app_prefs", MODE_PRIVATE) }

    companion object {
        private const val TAG = "CreateTourActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверка роли пользователя
        checkUserRole()

        // Настройка кнопки создания тура
        binding.createTourButton.setOnClickListener {
            createTour()
        }
    }

    private fun checkUserRole() {
        ApiClient.apiService.getCurrentUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Log.d(TAG, "User role: ${user.usertype}")
                        if (user.usertype != "guide") {
                            Toast.makeText(this@CreateTourActivity, "Only guides can create tours", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } ?: run {
                        Log.e(TAG, "User response body is null")
                        redirectToLogin()
                    }
                } else {
                    Log.e(TAG, "Failed to load user: ${response.code()} - ${response.message()}")
                    //redirectToLogin()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@CreateTourActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
        })
    }

    private fun createTour() {
        val title = binding.titleEditText.text.toString().trim()
        val price = binding.priceEditText.text.toString().trim()
        val location = binding.locationEditText.text.toString().trim()
        val date = binding.dateEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()

        if (title.isEmpty() || price.isEmpty() || location.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateTourRequest(title, price, location, date, description)
        ApiClient.apiService.createTour(request).enqueue(object : Callback<WebTours> {
            override fun onResponse(call: Call<WebTours>, response: Response<WebTours>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateTourActivity, "Tour created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "Failed to create tour: ${response.code()} - ${response.message()}")
                    if (response.code() == 403) {
                        Toast.makeText(this@CreateTourActivity, "Only guides can create tours", Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 401) {
                        redirectToLogin()
                    } else {
                        Toast.makeText(this@CreateTourActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<WebTours>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@CreateTourActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToLogin() {
        sharedPrefs.edit().remove("auth_token").apply()
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}