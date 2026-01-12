package com.bhaskar.pixelwalls

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.bhaskar.pixelwalls.presentation.navigation.AppNavigation

@Composable
fun App() {
    MaterialTheme {
        AppNavigation()
    }
}