package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget

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
    data class OnSubjectToggle(val enabled: Boolean): EditorUiEvents()
    data class OnSetWallpaperClick(val target: WallpaperTarget? = null) : EditorUiEvents()
    data object OnSaveToGalleryClick : EditorUiEvents()
    data object OnLocateWallpaperClick: EditorUiEvents()
    data class OnCaptured(val bytes: ByteArray) : EditorUiEvents()
    data object OnShareClick : EditorUiEvents()
    data object OnDismissDialog : EditorUiEvents()

    data object OnDownloadModel: EditorUiEvents()
}