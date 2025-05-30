package com.example.verst

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("name") val cityName: String
)

data class Main(
    @SerializedName("temp") val temperature: Double
)

data class Weather(
    @SerializedName("description") val description: String
)