package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.ModelStatus
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.ControlPage
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.ActionStep

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
    val isSubjectEnabled: Boolean = true,

    val bgColor: Color = Color(0xFFE6B34A),
    val shape: String = "Circle",

    val currentControlPage: ControlPage = ControlPage.ADJUST,

    val isControlPanelVisible: Boolean = true,
    val isColorPickerVisible: Boolean = false,

    // Platform Capabilities (Populated on init)
    val canSetWallpaperDirectly: Boolean = false,
    val canApplyInDifferentScreens: Boolean = false,
    val isShareSupported: Boolean = false,

    // Dialog State
    val showWallpaperDialog: Boolean = false,
    val currentActionStep: ActionStep = ActionStep.Main,
    val capturedBytes: ByteArray? = null,
    val wallpaperResult: WallpaperSetResult? = null,
    val isOperating: Boolean = false,

    // Model Status
    val modelStatus: ModelStatus = ModelStatus.Ready
)
