package com.bhaskar.pixelwalls.presentation.editor.controlPanel.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.ControlPage

data class Controls(
    val label: String,
    val page: ControlPage,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector
)
