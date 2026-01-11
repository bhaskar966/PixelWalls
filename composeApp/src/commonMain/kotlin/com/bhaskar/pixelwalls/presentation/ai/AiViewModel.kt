package com.bhaskar.pixelwalls.presentation.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.repository.AiRepository
import com.bhaskar.pixelwalls.domain.repository.EditorRepository
import com.bhaskar.pixelwalls.domain.repository.PromptTemplatesRepository
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.ActionStep
import com.bhaskar.pixelwalls.utils.platformAiAspectRatio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AiViewModel(
    private val aiRepository: AiRepository,
    private val editorRepository: EditorRepository
) : ViewModel() {

    private val _aiScreenUiState = MutableStateFlow(AiUiState())
    val aiScreenUiState = _aiScreenUiState.asStateFlow()

    init {
        val initialTemplate = PromptTemplatesRepository.templates[0]
        val initialSelection = initialTemplate.variables.mapValues { 0 }

        _aiScreenUiState.update {
            it.copy(
                isShareSupported = editorRepository.isShareSupported(),
                canApplyInDifferentScreens = editorRepository.canApplyWallpaperInDifferentScreens(),
                selectedTemplate = initialTemplate,
                promptSelection = initialSelection,
                aspectRatio = platformAiAspectRatio()
            )
        }
        updatePromptText()
    }

    fun onEvent(event: AiUiEvents) {
        when (event) {
            is AiUiEvents.UpdatePrompt -> _aiScreenUiState.update { it.copy(prompt = event.prompt) }
            is AiUiEvents.Generate -> generate()
            is AiUiEvents.OnActionClick -> _aiScreenUiState.update { it.copy(showWallpaperDialog = true) }
            is AiUiEvents.OnDismissDialog -> _aiScreenUiState.update { it.copy(showWallpaperDialog = false, currentActionStep = ActionStep.Main, wallpaperResult = null) }
            is AiUiEvents.OnSetWallpaper -> setWallpaper(event.target)
            is AiUiEvents.OnShareClick -> shareImage()
            is AiUiEvents.OnSaveToGalleryClick -> saveToGallery(false)
            is AiUiEvents.OnLocateClick -> locateWallpaper()
            is AiUiEvents.SelectTemplate -> {
                _aiScreenUiState.update {
                    it.copy(
                        selectedTemplate = event.template,
                        promptSelection = event.template.variables.mapValues { 0 }
                    )
                }
                updatePromptText()
            }

            is AiUiEvents.SelectCategory -> {
                val templatesInCategory = PromptTemplatesRepository.templates
                    .filter { it.category == event.category }
                if (templatesInCategory.isNotEmpty()) {
                    _aiScreenUiState.update {
                        it.copy(
                            selectedCategory = event.category,
                            selectedTemplate = templatesInCategory[0],
                            promptSelection = templatesInCategory[0].variables.mapValues { 0 }
                        )
                    }
                    updatePromptText()
                }
            }

            is AiUiEvents.OpenVariableEditor -> {
                _aiScreenUiState.update { it.copy(editingVariable = event.variableKey) }
            }

            AiUiEvents.CloseVariableEditor -> {
                _aiScreenUiState.update { it.copy(editingVariable = null) }
            }

            is AiUiEvents.SelectVariableOption -> {
                val currentSelection = _aiScreenUiState.value.promptSelection.toMutableMap()
                currentSelection[event.variableKey] = event.optionIndex
                _aiScreenUiState.update { it.copy(promptSelection = currentSelection, editingVariable = null) }
                updatePromptText()
            }

            is AiUiEvents.RandomizePrompt -> {
                val randomSelection = PromptTemplatesRepository.getRandomSelection(
                    aiScreenUiState.value.selectedTemplate.id
                )
                _aiScreenUiState.update { it.copy(promptSelection = randomSelection) }
                updatePromptText()
            }

            is AiUiEvents.OnToggleAspectRatioSelector -> {
                _aiScreenUiState.update { it.copy(isAspectRatioSelectorVisible = !it.isAspectRatioSelectorVisible) }
            }
            is AiUiEvents.OnUpdateAspectRatio -> {
                _aiScreenUiState.update { it.copy(aspectRatio = event.ratio) }
            }
        }
    }

    private fun generate() {
        val prompt = aiScreenUiState.value.prompt
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _aiScreenUiState.update {
                it.copy(
                    isGenerating = true,
                    error = null,
                    generatedImageBytes = null
                )
            }
            aiRepository.generateImage(prompt, aiScreenUiState.value.aspectRatio).fold(
                onSuccess = { bytes ->
                    _aiScreenUiState.update {
                        it.copy(
                            isGenerating = false,
                            generatedImageBytes = bytes,
                        )
                    }
                    saveToGallery(isAutoSave = true)
                },
                onFailure = { e ->
                    _aiScreenUiState.update {
                        it.copy(
                            isGenerating = false,
                            error = e.message
                        )
                    }
                }
            )
        }
    }

    private fun setWallpaper(target: WallpaperTarget?) {
        val bytes = aiScreenUiState.value.generatedImageBytes ?: return
        if (target == null && aiScreenUiState.value.canApplyInDifferentScreens) {
            _aiScreenUiState.update { it.copy(currentActionStep = ActionStep.TargetSelection) }
            return
        }
        viewModelScope.launch {
            _aiScreenUiState.update { it.copy(isOperating = true) }
            val result = editorRepository.setWallpaper(bytes, target)
            _aiScreenUiState.update { it.copy(isOperating = false, wallpaperResult = result, currentActionStep = ActionStep.Result) }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun saveToGallery(isAutoSave: Boolean) {
        val bytes = aiScreenUiState.value.generatedImageBytes ?: return
        val fileName = "AI_Wall_${Clock.System.now().toEpochMilliseconds()}"

        viewModelScope.launch {
            if (!isAutoSave) _aiScreenUiState.update { it.copy(isOperating = true) }

            editorRepository.saveToGallery(fileName, bytes).fold(
                onSuccess = {
                    if (!isAutoSave) {
                        _aiScreenUiState.update { it.copy(
                            isOperating = false,
                            currentActionStep = ActionStep.Result,
                            wallpaperResult = WallpaperSetResult.Success
                        ) }
                    }
                },
                onFailure = { error ->
                    if (!isAutoSave) {
                        _aiScreenUiState.update { it.copy(
                            isOperating = false,
                            currentActionStep = ActionStep.Result,
                            wallpaperResult = WallpaperSetResult.Error(error.message ?: "Save failed")
                        ) }
                    }
                }
            )
        }
    }


    private fun shareImage() {
        val bytes = aiScreenUiState.value.generatedImageBytes ?: return
        viewModelScope.launch {
            _aiScreenUiState.update { it.copy(isOperating = true) }

            editorRepository.shareImage("PixelWall_AI_Share", bytes).fold(
                onSuccess = {
                    _aiScreenUiState.update { it.copy(isOperating = false) }
                },
                onFailure = { error ->
                    _aiScreenUiState.update {
                        it.copy(
                            isOperating = false,
                            error = error.message ?: "Share failed"
                        )
                    }
                }
            )
        }
    }

    private fun locateWallpaper() {
        val bytes = aiScreenUiState.value.generatedImageBytes ?: return
        viewModelScope.launch {
            editorRepository.openWallpaperPicker(bytes)
        }
    }


    private fun updatePromptText() {
        val template = aiScreenUiState.value.selectedTemplate
        val selection = aiScreenUiState.value.promptSelection
        val promptText = PromptTemplatesRepository.buildPrompt(template, selection)
        _aiScreenUiState.update { it.copy(prompt = promptText) }
    }
}
