package com.bhaskar.pixelwalls.presentation.creations

import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.ActionStep

data class CreationsUiState(
    val wallpapers: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val needsPermission: Boolean = true,

    val canApplyInDifferentScreens: Boolean = false,
    val isShareSupported: Boolean = false,
    val selectedPreviewPath: String? = null,
    val showWallpaperDialog: Boolean = false,
    val currentActionStep: ActionStep = ActionStep.Main,
    val isOperating: Boolean = false,
    val wallpaperResult: WallpaperSetResult? = null,
)