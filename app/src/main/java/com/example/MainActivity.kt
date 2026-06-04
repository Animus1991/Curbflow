package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.LocalThemeManager
import com.example.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { 
      val systemDarkTheme = isSystemInDarkTheme()
      var isDark by remember { mutableStateOf(systemDarkTheme) }

      val themeManager = remember {
          object : ThemeManager {
              override fun toggleTheme() {
                  isDark = !isDark
              }
              override val isDarkTheme: Boolean
                  get() = isDark
          }
      }

      CompositionLocalProvider(LocalThemeManager provides themeManager) {
          MyApplicationTheme(darkTheme = isDark) { MainApp() } 
      }
    }
  }
}

