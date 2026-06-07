package com.example.ui.util

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    throw IllegalStateException("No ViewModelFactory provided")
}
