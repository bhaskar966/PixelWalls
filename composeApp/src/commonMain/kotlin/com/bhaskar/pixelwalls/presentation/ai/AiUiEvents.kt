package com.bhaskar.pixelwalls.presentation.ai

import com.bhaskar.pixelwalls.domain.model.AiAspectRatio
import com.bhaskar.pixelwalls.domain.model.PromptCategory
import com.bhaskar.pixelwalls.domain.model.PromptTemplate
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget

sealed class AiUiEvents {
    data class UpdatePrompt(val prompt: String) : AiUiEvents()
    data object Generate : AiUiEvents()
    data object OnActionClick : AiUiEvents()
    data object OnDismissDialog : AiUiEvents()
    data class OnSetWallpaper(val target: WallpaperTarget?) : AiUiEvents()
    data object OnShareClick : AiUiEvents()
    data object OnSaveToGalleryClick : AiUiEvents()
    data object OnLocateClick : AiUiEvents()

    data class SelectTemplate(val template: PromptTemplate) : AiUiEvents()
    data class SelectCategory(val category: PromptCategory) : AiUiEvents()
    data class OpenVariableEditor(val variableKey: String) : AiUiEvents()
    data object CloseVariableEditor : AiUiEvents()
    data class SelectVariableOption(val variableKey: String, val optionIndex: Int) : AiUiEvents()
    data object RandomizePrompt : AiUiEvents()
    data class OnUpdateAspectRatio(val ratio: AiAspectRatio) : AiUiEvents()
    data object OnToggleAspectRatioSelector : AiUiEvents()

}
