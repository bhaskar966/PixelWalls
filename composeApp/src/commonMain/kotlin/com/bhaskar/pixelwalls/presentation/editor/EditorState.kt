package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap

data class EditorState(
    val originalImageUri: String? = null,
    val subjectImageUri: String? = null,
    val subjectBounds: Rect = Rect.Zero,
    val isLoading: Boolean = false,
    val error: String? = null
)
