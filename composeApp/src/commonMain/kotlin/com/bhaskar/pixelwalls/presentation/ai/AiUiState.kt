package com.bhaskar.pixelwalls.presentation.ai

import com.bhaskar.pixelwalls.domain.model.AiAspectRatio
import com.bhaskar.pixelwalls.domain.model.PromptCategory
import com.bhaskar.pixelwalls.domain.model.PromptTemplate
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.repository.PromptTemplatesRepository
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.ActionStep

data class AiUiState(
    val prompt: String = "",
    val isGenerating: Boolean = false,
    val generatedImageBytes: ByteArray? = null,
    val error: String? = null,

    val isShareSupported: Boolean = false,
    val canApplyInDifferentScreens: Boolean = false,

    // Dialog state
    val showWallpaperDialog: Boolean = false,
    val isOperating: Boolean = false,
    val currentActionStep: ActionStep = ActionStep.Main,
    val wallpaperResult: WallpaperSetResult? = null,

    val selectedTemplate: PromptTemplate = PromptTemplatesRepository.templates[0],
    val promptSelection: Map<String, Int> = emptyMap(),
    val editingVariable: String? = null,
    val selectedCategory: PromptCategory = PromptCategory.IMAGINARY,
    val aspectRatio: AiAspectRatio = AiAspectRatio.SQUARE,
    val isAspectRatioSelectorVisible: Boolean = false
)
