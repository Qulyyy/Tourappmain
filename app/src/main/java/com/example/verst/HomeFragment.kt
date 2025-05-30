package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.verst.databinding.FragmentHomeBinding
import com.example.verst.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val sharedPrefs by lazy { requireContext().getSharedPreferences("tour_app_prefs", android.content.Context.MODE_PRIVATE) }

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка имени пользователя с сервера
        fetchUserName()

        // Настройка заголовка "Beautiful world!"
        val titleText = "Beautiful world!"
        val spannableString = SpannableString(titleText)
        val beautifulStart = titleText.indexOf("world!")
        val beautifulEnd = beautifulStart + "world!".length
        spannableString.setSpan(
            ForegroundColorSpan(android.graphics.Color.parseColor("#FF7029")),
            beautifulStart,
            beautifulEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.titleExplore1.text = spannableString

        // Настройка кнопки "View All"
        binding.viewAllButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        // Настройка перехода на ProfileActivity при клике на аватар
        binding.avatar.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        // Настройка уведомлений
        binding.notificationButton.setOnClickListener {
            Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT).show()
        }

        // Настройка RecyclerView
        binding.destinationsRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Загрузка туров с сервера
        fetchTours()
    }

    private fun fetchUserName() {
        binding.userName.text = "Guest"
        ApiClient.apiService.getCurrentUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Log.d(TAG, "User login: ${user.login}")
                        binding.userName.text = user.login
                    } ?: run {
                        Log.e(TAG, "User response body is null")
                        binding.userName.text = "Guest"
                        Toast.makeText(requireContext(), "Failed to load user name", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to load user: ${response.code()} - ${response.message()}")
                    binding.userName.text = "Guest"
                    when (response.code()) {
                        401, 403 -> redirectToLogin()
                        else -> Toast.makeText(requireContext(), "Error loading user: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                binding.userName.text = "Guest"
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTours() {
        ApiClient.apiService.getAllTours().enqueue(object : Callback<List<WebTours>> {
            override fun onResponse(call: Call<List<WebTours>>, response: Response<List<WebTours>>) {
                if (response.isSuccessful) {
                    response.body()?.let { tours ->
                        Log.d(TAG, "Received ${tours.size} tours")
                        binding.destinationsRecyclerView.adapter = DestinationAdapter(tours) { tour ->
                            val intent = Intent(requireContext(), TourActivity::class.java).apply {
                                putExtra("tour_id", tour.id)
                            }
                            startActivity(intent)
                        }
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        Toast.makeText(requireContext(), "No tours found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to load tours: ${response.code()} - ${response.message()}")
                    when (response.code()) {
                        401, 403 -> redirectToLogin()
                        else -> Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<WebTours>>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToLogin() {
        sharedPrefs.edit().remove("auth_token").apply()
        Toast.makeText(requireContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
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