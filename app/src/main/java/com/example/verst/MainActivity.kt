package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.verst.databinding.ActivityMainBinding
import com.example.verst.network.ApiClient
import retrofit2.Callback
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

import android.widget.Toast

import com.example.verst.network.UserRemote
import retrofit2.Call

import retrofit2.Response

data class DayItem(
    val date: LocalDate,
    val letter: String,
    val number: String,
    var isSelected: Boolean = false
)

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastFragment: Fragment? = null
    private val sharedPrefs by lazy { getSharedPreferences("tour_app_prefs", MODE_PRIVATE) }

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            updateSelectedState(binding.customBottomNavigation.navHome)
        }

        binding.customBottomNavigation.navHome.setOnClickListener {
            replaceFragment(HomeFragment())
            updateSelectedState(it)
        }

        binding.customBottomNavigation.navCalendar.setOnClickListener {
            replaceFragment(CalendarFragment())
            updateSelectedState(it)
        }

        binding.customBottomNavigation.navSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.customBottomNavigation.navMessages.setOnClickListener {
            handleNavTourClick()
        }

        binding.customBottomNavigation.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun handleNavTourClick() {
        val token = sharedPrefs.getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return
        }

        Log.d(TAG, "Fetching user data for navTour action")
        ApiClient.apiService.getCurrentUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Log.d(TAG, "User usertype: ${user.usertype}")
                        val intent = when (user.usertype) {
                            "tourist" -> Intent(this@MainActivity, OrderToursActivity::class.java)
                            "guide" -> Intent(this@MainActivity, CreateTourActivity::class.java)
                            else -> {
                                Log.w(TAG, "Unknown usertype: ${user.usertype}")
                                Toast.makeText(this@MainActivity, "Unknown user type", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                        startActivity(intent)
                    } ?: run {
                        Log.e(TAG, "User response body is null")
                        redirectToLogin()
                    }
                } else {
                    Log.e(TAG, "Failed to load user: ${response.code()} - ${response.message()}")
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@MainActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToLogin() {
        Log.d(TAG, "Redirecting to LoginActivity")
        sharedPrefs.edit().remove("auth_token").apply()
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("FragmentSwitch", "Switching to ${fragment.javaClass.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        lastFragment = fragment
    }

    private fun updateSelectedState(selectedView: View) {
        listOf(
            binding.customBottomNavigation.navHome,
            binding.customBottomNavigation.navCalendar,
            binding.customBottomNavigation.navMessages,
            binding.customBottomNavigation.navProfile
        ).forEach { view ->
            val layout = view as LinearLayout
            val icon = layout.getChildAt(0) as ImageView
            val text = layout.getChildAt(1) as TextView
            icon.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))
            text.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }

        if (selectedView != binding.customBottomNavigation.navSearch) {
            val selectedLayout = selectedView as LinearLayout
            val selectedIcon = selectedLayout.getChildAt(0) as ImageView
            val selectedText = selectedLayout.getChildAt(1) as TextView
            selectedIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
            selectedText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
        }
    }
}