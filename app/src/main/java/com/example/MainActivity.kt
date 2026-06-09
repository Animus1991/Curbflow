package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.theme.AccessibilityPrefs
import com.example.ui.theme.LocalAccessibilityPrefs
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.LocalThemeManager
import com.example.ui.theme.ThemeManager

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ParkingDatabase
import com.example.util.ViewModelFactory

import com.example.ui.util.LocalViewModelFactory

class MainActivity : ComponentActivity() {

  private val locationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { /* App functions in degraded (no-GPS) mode if denied; map and zones still work */ }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    locationPermissionLauncher.launch(
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
      )
    )
    setContent { 
      val systemDarkTheme = isSystemInDarkTheme()
      var isDark by remember { mutableStateOf(systemDarkTheme) }

      val context = LocalContext.current
      val database = remember { ParkingDatabase.getDatabase(context) }
      val viewModelFactory = remember { ViewModelFactory(context, database) }

      // Read accessibility preferences from SharedPreferences (live-reactive)
      val prefs = remember { context.getSharedPreferences("curbflow_prefs", android.content.Context.MODE_PRIVATE) }
      var accessibilityPrefs by remember {
          mutableStateOf(
              AccessibilityPrefs(
                  colorblindMode = prefs.getBoolean("colorblind_mode", false),
                  highContrast = prefs.getBoolean("high_contrast", false),
                  largeText = prefs.getBoolean("large_text", false)
              )
          )
      }
      // Re-read on every recomposition triggered by SettingsScreen toggling
      val listener = remember {
          android.content.SharedPreferences.OnSharedPreferenceChangeListener { sp, _ ->
              accessibilityPrefs = AccessibilityPrefs(
                  colorblindMode = sp.getBoolean("colorblind_mode", false),
                  highContrast = sp.getBoolean("high_contrast", false),
                  largeText = sp.getBoolean("large_text", false)
              )
          }
      }
      androidx.compose.runtime.DisposableEffect(prefs) {
          prefs.registerOnSharedPreferenceChangeListener(listener)
          onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
      }

      val themeManager = remember {
          object : ThemeManager {
              override fun toggleTheme() {
                  isDark = !isDark
              }
              override val isDarkTheme: Boolean
                  get() = isDark
          }
      }

      CompositionLocalProvider(
          LocalThemeManager provides themeManager,
          LocalViewModelFactory provides viewModelFactory,
          LocalAccessibilityPrefs provides accessibilityPrefs
      ) {
          MyApplicationTheme(darkTheme = isDark, accessibilityPrefs = accessibilityPrefs) { MainApp() } 
      }
    }
  }
}

