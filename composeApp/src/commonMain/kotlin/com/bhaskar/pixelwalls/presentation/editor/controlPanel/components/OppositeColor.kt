package com.bhaskar.pixelwalls.presentation.editor.controlPanel.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun Color.oppositeColor(): Color {
    return remember(this) {
        val luminance = (0.299 * this.red + 0.587 * this.green + 0.114 * this.blue)
        if(luminance > 0.5) Color.Black else Color.White
    }
}