package com.example.verst

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    var darkTheme = false
        private set
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        val sharedPrefs = getSharedPreferences("tour_app_prefs", MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean("dark_theme", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        val sharedPrefs = getSharedPreferences("tour_app_prefs", MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("dark_theme", darkThemeEnabled).apply()
    }
}