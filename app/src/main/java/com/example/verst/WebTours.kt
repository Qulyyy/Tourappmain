package com.example.verst

import kotlinx.serialization.Serializable

@Serializable
data class WebTours(
    val id: Int = 0,
    val title: String = "",
    val price: String = "",
    val location: String = "",
    val imageResId: String = "", // Имя ресурса или путь
    val date: String = "", // Формат ISO, например, "2025-06-01"
    val description: String = "", // Описание тура
    val guideLogin: String
)