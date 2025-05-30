package com.example.verst.network

import com.example.verst.User
import com.example.verst.WebTours
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import kotlinx.serialization.Serializable
import retrofit2.http.PUT

@Serializable
data class LoginRemote(val login: String, val password: String)

@Serializable
data class LoginReceiveRemote(val token: String)

@Serializable
data class RegisterRemote(val login: String, val email: String, val password: String)

@Serializable
data class RegisterReceiveRemote(val token: String)

@Serializable
data class UserRemote(val login: String)

@Serializable
data class UserTourRequest(val tourId: Int)

@Serializable
data class CreateTourRequest(
    val title: String,
    val price: String,
    val location: String,
    val date: String,
    val description: String
)

@Serializable
data class UpdateUserRequest(
    val login: String,
    val email: String,
    val password: String? = null
)

interface ApiService {
    @POST("api/auth/register")
    fun register(@Body request: RegisterRemote): Call<RegisterReceiveRemote>

    @POST("api/auth/login")
    fun login(@Body request: LoginRemote): Call<LoginReceiveRemote>

    @GET("tours")
    fun getAllTours(): Call<List<WebTours>>

    @GET("tours/{id}")
    fun getTourById(@Path("id") id: Int): Call<WebTours>

    @GET("api/user")
    fun getCurrentUser(): Call<User>

    @POST("api/user_tours")
    fun purchaseTour(@Body request: UserTourRequest): Call<Void>

    @GET("api/user_tours")
    fun getPurchasedTours(): Call<List<WebTours>>

    @POST("tours")
    fun createTour(@Body request: CreateTourRequest): Call<WebTours>

    @PUT("/api/user")
    fun updateUser(@Body request: UpdateUserRequest): Call<User>
}