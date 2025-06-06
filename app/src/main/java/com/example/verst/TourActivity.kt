package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.verst.databinding.ActivityTourBinding
import com.example.verst.network.ApiClient
import com.example.verst.network.UserTourRequest
import com.example.verst.network.WeatherApiClient
import com.example.verst.network.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TourActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTourBinding
    private val sharedPrefs by lazy { getSharedPreferences("tour_app_prefs", MODE_PRIVATE) }
    private var tourId: Int = -1
    private var currentLocation: String? = null
    private val weatherApiKey = "b3649b59d6b0b30d01078dd4255d4cc2" // Замените на ваш ключ OpenWeatherMap

    companion object {
        private const val TAG = "TourActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTourBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tourId = intent.getIntExtra("tour_id", -1)
        if (tourId == -1) {
            Toast.makeText(this, "Invalid tour ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Загрузка деталей тура
        fetchTourDetails()

        // Настройка кнопки покупки
        binding.buyButton.setOnClickListener {
            purchaseTour()
        }

        // Настройка кнопки обновления погоды
        binding.refreshButton.setOnClickListener {
            currentLocation?.let { fetchWeather(normalizeCityName(it)) }
        }
    }

    private fun fetchTourDetails() {
        ApiClient.apiService.getTourById(tourId).enqueue(object : Callback<WebTours> {
            override fun onResponse(call: Call<WebTours>, response: Response<WebTours>) {
                if (response.isSuccessful) {
                    response.body()?.let { tour ->
                        Log.d(TAG, "Tour loaded: ${tour.title}")
                        binding.tourTitle.text = tour.title
                        binding.tourLocationIconText.text = tour.location
                        binding.tourPrice.text = "${tour.price} Р"
                        binding.tourDescription.text = tour.description
                        binding.tourDate.text = tour.date
                        // Сохраняем локацию и загружаем погоду
                        currentLocation = tour.location
                        fetchWeather(normalizeCityName(tour.location))
                    } ?: run {
                        Log.e(TAG, "Tour response body is null")
                        Toast.makeText(this@TourActivity, "Failed to load tour", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to load tour: ${response.code()} - ${response.message()}")
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin()
                    } else {
                        Toast.makeText(this@TourActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<WebTours>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@TourActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun normalizeCityName(city: String): String {
        return when (city.lowercase()) {
            "москва" -> "Moscow"
            "сочи" -> "Sochi"
            "рязань" -> "Ryazan"
            else -> city
        }
    }

    private fun translateWeatherDescription(description: String): String {
        return when (description.lowercase()) {
            "clear sky" -> "Ясно"
            "few clouds" -> "Малооблачно"
            "scattered clouds" -> "Рассеянные облака"
            "broken clouds" -> "Облачно"
            "shower rain" -> "Ливень"
            "rain" -> "Дождь"
            "thunderstorm" -> "Гроза"
            "snow" -> "Снег"
            "mist" -> "Туман"
            "overcast clouds" -> "Пасмурно"
            else -> description.replaceFirstChar { it.uppercase() }
        }
    }

    private fun fetchWeather(city: String) {
        binding.weatherProgress.visibility = View.VISIBLE
        binding.weatherInfo.text = "Погода: Загрузка..."

        WeatherApiClient.weatherApiService.getWeather(city, weatherApiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                binding.weatherProgress.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.let { weather ->
                        val temperature = weather.main.temp
                        val description = weather.weather.firstOrNull()?.description?.let { translateWeatherDescription(it) } ?: "Неизвестно"
                        binding.weatherInfo.text = "Погода: $temperature°C, $description"
                        Log.d(TAG, "Weather loaded for $city: $temperature°C, $description")
                    } ?: run {
                        binding.weatherInfo.text = "Погода: Данные недоступны"
                        Toast.makeText(this@TourActivity, "Данные о погоде недоступны", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.weatherInfo.text = "Погода: Ошибка"
                    Log.e(TAG, "Failed to load weather: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@TourActivity, "Ошибка загрузки погоды: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                binding.weatherProgress.visibility = View.GONE
                binding.weatherInfo.text = "Погода: Ошибка сети"
                Log.e(TAG, "Weather network error: ${t.message}", t)
                Toast.makeText(this@TourActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun purchaseTour() {
        val request = UserTourRequest(tourId)
        ApiClient.apiService.purchaseTour(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@TourActivity, "Tour purchased", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Failed to purchase tour: ${response.code()} - ${response.message()}")
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin()
                    } else if (response.code() == 409) {
                        Toast.makeText(this@TourActivity, "Tour already purchased", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@TourActivity, "Error purchasing tour: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@TourActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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