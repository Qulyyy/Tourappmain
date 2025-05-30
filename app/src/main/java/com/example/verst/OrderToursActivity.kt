package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.verst.databinding.ActivityOrderToursBinding
import com.example.verst.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderToursActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderToursBinding
    private val sharedPrefs by lazy { getSharedPreferences("tour_app_prefs", MODE_PRIVATE) }

    companion object {
        private const val TAG = "OrderToursActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderToursBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка RecyclerView
        binding.gridView.layoutManager = GridLayoutManager(this, 2)

        // Загрузка купленных туров
        fetchPurchasedTours()
    }

    private fun fetchPurchasedTours() {
        ApiClient.apiService.getPurchasedTours().enqueue(object : Callback<List<WebTours>> {
            override fun onResponse(call: Call<List<WebTours>>, response: Response<List<WebTours>>) {
                if (response.isSuccessful) {
                    response.body()?.let { tours ->
                        Log.d(TAG, "Received ${tours.size} purchased tours")
                        binding.gridView.adapter = OrderTourAdapter(tours)
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        Toast.makeText(this@OrderToursActivity, "No purchased tours found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to load purchased tours: ${response.code()} - ${response.message()}")
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin()
                    } else {
                        Toast.makeText(this@OrderToursActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<WebTours>>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@OrderToursActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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