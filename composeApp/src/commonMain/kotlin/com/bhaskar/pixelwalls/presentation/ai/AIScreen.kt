package com.bhaskar.pixelwalls.presentation.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AIScreen(
    onGenerateClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "✨ AI Wallpapers",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Generate wallpapers with AI\n(Coming soon)",
            style = MaterialTheme.typography.bodyLarge
        )

        Button(onClick = onGenerateClick) {
            Text("Generate AI Wallpaper")
        }
    }
}
