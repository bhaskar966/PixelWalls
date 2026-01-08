package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.ui.graphics.Color

sealed class EditorUiEvents {

    data class OnImageSelect(val imageBytes: ByteArray): EditorUiEvents()
    data class OnScaleChange(val scale: Float) : EditorUiEvents()
    data class OnOffsetChange(val offsetX: Float, val offsetY: Float) : EditorUiEvents()
    data class OnShapeRadiusChange(val percent: Float) : EditorUiEvents()
    data class OnClipHeightChange(val percent: Float) : EditorUiEvents()
    data class OnHollowYChange(val percent: Float) : EditorUiEvents()
    data class OnBgColorChange(val color: Color) : EditorUiEvents()
    data class OnShapeChange(val shape: String) : EditorUiEvents()
    data object OnControlPanelToggle : EditorUiEvents()
    data class OnColorPickerToggle(val visible: Boolean) : EditorUiEvents()

}