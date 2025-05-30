package com.example.verst

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int = 0,
    val login: String,
    val email: String,
    val password: String,
    val usertype: String
)