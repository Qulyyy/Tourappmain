package com.example.verst

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.verst.databinding.ActivitySearchBinding
import com.example.verst.network.ApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val filteredTours = mutableListOf<WebTours>()
    private lateinit var adapter: ToursAdapter
    private var lastQuery: String? = null
    private var isLoading: Boolean = false
    private var isError: Boolean = false
    private var isRedirecting: Boolean = false // Флаг для предотвращения множественных перенаправлений

    // Search history
    private val searchHistory = mutableListOf<String>()
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private val sharedPrefs by lazy { getSharedPreferences("tour_app_prefs", MODE_PRIVATE) }
    private val historyKey = "search_history"

    // Debounce for automatic search
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        val query = binding.searchBar.text.toString()
        if (query.isNotEmpty()) {
            filterTours(query)
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L // 2 seconds
        private const val TAG = "SearchActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log token for debugging
        val token = sharedPrefs.getString("auth_token", null)
        Log.d(TAG, "Auth token on create: $token")

        // Check if token exists
        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return
        }

        // Initialize adapter for tours
        adapter = ToursAdapter(filteredTours, { tour: WebTours ->
            lastQuery?.let { query ->
                if (query.isNotEmpty()) {
                    addToSearchHistory(query)
                }
            }
            val intent = Intent(this, TourActivity::class.java).apply {
                putExtra("tour_id", tour.id) // Добавляем ID тура
                putExtra("tour_title", tour.title)
                putExtra("tour_price", tour.price)
                putExtra("tour_location", tour.location)
            }
            startActivity(intent)
        }, isLoading, isError, { retryLastQuery() })

        binding.toursList.layoutManager = GridLayoutManager(this, 2)
        binding.toursList.adapter = adapter

        // Initialize adapter for search history
        searchHistoryAdapter = SearchHistoryAdapter(searchHistory) { query ->
            binding.searchBar.setText(query)
            filterTours(query)
            binding.searchHistoryList.visibility = View.GONE
        }
        binding.searchHistoryList.layoutManager = LinearLayoutManager(this)
        binding.searchHistoryList.adapter = searchHistoryAdapter

        // Load search history
        loadSearchHistory()

        // Restore state
        val savedQuery = savedInstanceState?.getString("search_query")
        savedInstanceState?.getStringArrayList("search_history")?.let { history ->
            searchHistory.clear()
            searchHistory.addAll(history)
            searchHistoryAdapter.notifyDataSetChanged()
            binding.clearHistoryButton.visibility = if (searchHistory.isNotEmpty()) View.VISIBLE else View.GONE
            binding.searchHistoryList.visibility = if (searchHistory.isNotEmpty()) View.VISIBLE else View.GONE
        }

        // Back button to log out
        binding.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Clear button
        binding.clearButton.setOnClickListener {
            binding.searchBar.text.clear()
            binding.clearButton.visibility = View.GONE
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
            handler.removeCallbacks(searchRunnable)
            filterTours("")
        }

        // Clear history button
        binding.clearHistoryButton.setOnClickListener {
            clearSearchHistory()
        }

        // Search button
        binding.searchButton.setOnClickListener {
            val query = binding.searchBar.text.toString()
            if (query.isNotEmpty()) {
                handler.removeCallbacks(searchRunnable)
                filterTours(query)
            }
        }

        // Search bar focus
        binding.searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.searchBar, InputMethodManager.SHOW_IMPLICIT)
                if (searchHistory.isNotEmpty()) {
                    binding.searchHistoryList.visibility = View.VISIBLE
                    binding.clearHistoryButton.visibility = View.VISIBLE
                }
            } else {
                binding.searchHistoryList.visibility = View.GONE
                binding.clearHistoryButton.visibility = View.GONE
            }
        }

        // Search bar text change listener
        binding.searchBar.addTextChangedListener { text ->
            binding.clearButton.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            if (text.isNullOrEmpty()) {
                handler.removeCallbacks(searchRunnable)
                filterTours("")
            } else {
                searchDebounce()
            }
        }

        // Theme switcher
        val app = applicationContext as App
        binding.themeSwitcher.isChecked = app.darkTheme
        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            app.switchTheme(isChecked)
        }

        // Restore search bar text
        if (!savedQuery.isNullOrEmpty()) {
            binding.searchBar.setText(savedQuery)
            binding.clearButton.visibility = View.VISIBLE
            filterTours(savedQuery)
        } else {
            filterTours("")
        }
    }

    private fun filterTours(query: String) {
        Log.d(TAG, "Filtering tours with query: $query")
        lastQuery = query
        if (query.isNotEmpty()) {
            addToSearchHistory(query)
        }
        filteredTours.clear()
        isLoading = true
        isError = false
        adapter.notifyDataSetChanged()

        // Log token before request
        val token = sharedPrefs.getString("auth_token", null)
        Log.d(TAG, "Auth token before request: $token")

        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return
        }

        try {
            ApiClient.apiService.getAllTours().enqueue(object : Callback<List<WebTours>> {
                override fun onResponse(call: Call<List<WebTours>>, response: Response<List<WebTours>>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        response.body()?.let { tours ->
                            Log.d(TAG, "Received ${tours.size} tours")
                            filteredTours.addAll(tours.map {
                                WebTours(
                                    id = it.id,
                                    title = it.title,
                                    location = it.location,
                                    price = it.price,
                                    imageResId = it.imageResId.ifEmpty { "niladri_reservoir" },
                                    date = it.date,
                                    description = it.description,
                                    guideLogin = it.guideLogin
                                )
                            }.filter {
                                query.isEmpty() ||
                                        it.title.contains(query, ignoreCase = true) ||
                                        it.location.contains(query, ignoreCase = true)
                            })
                            adapter.notifyDataSetChanged()
                        } ?: run {
                            isError = true
                            Log.e(TAG, "Response body is null")
                            Toast.makeText(this@SearchActivity, "No tours found", Toast.LENGTH_SHORT).show()
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        isError = true
                        Log.e(TAG, "Failed to load tours: ${response.code()} - ${response.message()}")
                        val errorMessage = when (response.code()) {
                            401, 403 -> {
                                redirectToLogin()
                                "Unauthorized access. Please log in again."
                            }
                            404 -> "Tours endpoint not found. Check server configuration."
                            else -> "Failed to load tours: ${response.code()}"
                        }
                        Toast.makeText(this@SearchActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<List<WebTours>>, t: Throwable) {
                    isLoading = false
                    isError = true
                    Log.e(TAG, "Network error: ${t.message}", t)
                    Toast.makeText(this@SearchActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception in filterTours: ${e.message}", e)
            isLoading = false
            isError = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            adapter.notifyDataSetChanged()
        }
    }

    private fun redirectToLogin() {
        if (isRedirecting) return
        isRedirecting = true
        sharedPrefs.edit().remove("auth_token").apply()
        Log.d(TAG, "Redirecting to LoginActivity due to invalid or missing token")
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun retryLastQuery() {
        lastQuery?.let { query ->
            filterTours(query)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("search_query", binding.searchBar.text.toString())
        outState.putStringArrayList("search_history", ArrayList(searchHistory))
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun addToSearchHistory(query: String) {
        searchHistory.remove(query)
        searchHistory.add(0, query)
        if (searchHistory.size > 10) {
            searchHistory.removeAt(searchHistory.size - 1)
        }
        saveSearchHistory()
        searchHistoryAdapter.notifyDataSetChanged()
        binding.clearHistoryButton.visibility = if (searchHistory.isNotEmpty()) View.VISIBLE else View.GONE
        if (binding.searchBar.hasFocus() && searchHistory.isNotEmpty()) {
            binding.searchHistoryList.visibility = View.VISIBLE
        }
    }

    private fun saveSearchHistory() {
        val editor = sharedPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(searchHistory)
        editor.putString(historyKey, json)
        editor.apply()
    }

    private fun loadSearchHistory() {
        val json = sharedPrefs.getString(historyKey, null)
        if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<String>>() {}.type
            val loadedHistory: MutableList<String> = gson.fromJson(json, type)
            searchHistory.clear()
            searchHistory.addAll(loadedHistory)
            searchHistoryAdapter.notifyDataSetChanged()
            binding.clearHistoryButton.visibility = if (searchHistory.isNotEmpty()) View.VISIBLE else View.GONE
            if (binding.searchBar.hasFocus() && searchHistory.isNotEmpty()) {
                binding.searchHistoryList.visibility = View.VISIBLE
            }
        }
    }

    private fun clearSearchHistory() {
        searchHistory.clear()
        saveSearchHistory()
        searchHistoryAdapter.notifyDataSetChanged()
        binding.clearHistoryButton.visibility = View.GONE
        binding.searchHistoryList.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}