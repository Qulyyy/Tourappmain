package com.example.verst

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.verst.databinding.FragmentProfileBinding
import com.example.verst.network.ApiClient
import com.example.verst.network.UpdateUserRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val sharedPrefs by lazy { requireContext().getSharedPreferences("tour_app_prefs", Context.MODE_PRIVATE) }

    companion object {
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загрузка текущих данных пользователя
        fetchUserData()

        // Обработчик кнопки сохранения
        binding.saveButton.setOnClickListener {
            updateUserData()
        }
    }

    private fun fetchUserData() {
        ApiClient.apiService.getCurrentUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Log.d(TAG, "User data: login=${user.login}, email=${user.email}")
                        binding.loginEdit.setText(user.login)
                        binding.emailEdit.setText(user.email)
                        // Пароль не заполняем для безопасности
                    } ?: run {
                        Log.e(TAG, "User response body is null")
                        Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to load user: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserData() {
        val login = binding.loginEdit.text.toString().trim()
        val email = binding.emailEdit.text.toString().trim()
        val password = binding.passwordEdit.text.toString().trim()

        if (login.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Login and email are required", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UpdateUserRequest(
            login = login,
            email = email,
            password = if (password.isNotEmpty()) password else null
        )

        ApiClient.apiService.updateUser(updateRequest).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Log.d(TAG, "User updated: login=${user.login}, email=${user.email}")
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        // Перенаправляем на логин после успешного обновления
                        redirectToLogin()
                    } ?: run {
                        Log.e(TAG, "Update response body is null")
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to update user: ${response.code()} - ${response.message()}")
                    val errorMessage = when (response.code()) {
                        400 -> "Login or email already exists"
                        else -> "Error: ${response.message()}"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToLogin() {
        // Очищаем auth_token
        sharedPrefs.edit().remove("auth_token").apply()
        // Переходим на LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}