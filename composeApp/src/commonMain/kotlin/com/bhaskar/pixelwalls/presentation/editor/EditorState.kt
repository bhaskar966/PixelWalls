package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

data class EditorState(
    val isLoading: Boolean = false,
    val originalBitmap: ImageBitmap? = null,
    val originalImageUri: String? = null,
    val subjectImageUri: String? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val error: String? = null,

    // Manual control states
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val shapeRadiusPercent: Float = 0.5f,
    val clipHeightPercent: Float = 0.7f,
    val hollowCenterYPercent: Float = 0.3f,

    val bgColor: Color = Color(0xFFE6B34A),
    val shape: String = "Circle",

    val isControlPanelVisible: Boolean = true,
    val isColorPickerVisible: Boolean = false
)
