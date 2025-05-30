package com.example.verst

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.verst.databinding.ActivityProfileBinding
import com.example.verst.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val sharedPrefs by lazy { getSharedPreferences("tour_app_prefs", MODE_PRIVATE) }

    companion object {
        private const val TAG = "ProfileActivity"
        private const val KEY_CURRENT_FRAGMENT = "current_fragment"
        private const val KEY_MENU_VISIBLE = "menu_visible"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверка наличия fragment_container
        if (findViewById<FrameLayout>(R.id.fragment_container) == null) {
            Log.e(TAG, "Fragment container not found in layout")
            Toast.makeText(this, "Layout error: Fragment container missing", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val token = sharedPrefs.getString("auth_token", null)
        Log.d(TAG, "Auth token: $token")

        if (token.isNullOrEmpty()) {
            redirectToLogin()
            return
        }

        // Восстановление состояния
        if (savedInstanceState == null) {
            // Начальное состояние: показываем меню, скрываем фрагмент
            binding.menuOptions.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.GONE
        } else {
            val isMenuVisible = savedInstanceState.getBoolean(KEY_MENU_VISIBLE, true)
            binding.menuOptions.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
            binding.fragmentContainer.visibility = if (isMenuVisible) View.GONE else View.VISIBLE

            if (!isMenuVisible) {
                when (savedInstanceState.getString(KEY_CURRENT_FRAGMENT)) {
                    "settings" -> replaceFragment(SettingsFragment())
                    "version" -> replaceFragment(VersionFragment())
                    "profile" -> replaceFragment(ProfileFragment())
                }
            }
        }

        // Обработчики кликов
        binding.backButton.setOnClickListener { finish() }
        binding.logout.setOnClickListener { redirectToLogin() }
        binding.optionProfile.setOnClickListener {
            binding.menuOptions.visibility = View.GONE
            binding.fragmentContainer.visibility = View.VISIBLE
            replaceFragment(ProfileFragment())
        }
        binding.optionSettings.setOnClickListener {
            binding.menuOptions.visibility = View.GONE
            binding.fragmentContainer.visibility = View.VISIBLE
            replaceFragment(SettingsFragment())
        }
        binding.optionVersion.setOnClickListener {
            binding.menuOptions.visibility = View.GONE
            binding.fragmentContainer.visibility = View.VISIBLE
            replaceFragment(VersionFragment())
        }

        // Запрос данных пользователя
        fetchUserData()
    }

    private fun fetchUserData() {
        ApiClient.apiService.getCurrentUser().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        Log.d(TAG, "User data: login=${user.login}, email=${user.email}")
                        binding.profileName.text = user.login
                        binding.profileEmail.text = user.email
                    } ?: run {
                        Log.e(TAG, "User response body is null")
                        Toast.makeText(this@ProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Failed to load user: ${response.code()} - ${response.message()}")
                    if (response.code() == 401 || response.code() == 403) {
                        redirectToLogin()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@ProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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
        Log.d(TAG, "Replacing fragment with ${fragment.javaClass.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_MENU_VISIBLE, binding.menuOptions.visibility == View.VISIBLE)
        if (binding.fragmentContainer.visibility == View.VISIBLE) {
            val currentFragment = supportFragmentManager.fragments.firstOrNull()
            when (currentFragment) {
                is SettingsFragment -> outState.putString(KEY_CURRENT_FRAGMENT, "settings")
                is VersionFragment -> outState.putString(KEY_CURRENT_FRAGMENT, "version")
                is ProfileFragment -> outState.putString(KEY_CURRENT_FRAGMENT, "profile")
            }
        }
    }
}