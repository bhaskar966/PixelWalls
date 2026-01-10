package com.bhaskar.pixelwalls

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val window = this.window
            val controller = WindowInsetsControllerCompat(
                window,
                window.decorView
                )

            controller.isAppearanceLightStatusBars = true
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}