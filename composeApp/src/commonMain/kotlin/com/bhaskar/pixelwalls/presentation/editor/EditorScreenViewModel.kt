package com.bhaskar.pixelwalls.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import com.bhaskar.pixelwalls.utils.editor.toImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EditorScreenViewModel(
    private val backgroundRemover: BackgroundRemover,
    private val saveService: ImageSaveService,
    private val modelStatusService: ModelStatusService
): ViewModel() {

    private val _editorUiState = MutableStateFlow(EditorState())
    val editorUiState = _editorUiState.asStateFlow()

    init {
        modelStatusService.checkStatus()
    }

    fun onEvent(event: EditorUiEvents) {
        when(event) {
            is EditorUiEvents.OnImageSelect -> {
                processAndCacheImage(imageBytes = event.imageBytes)
            }
            is EditorUiEvents.OnScaleChange -> {
                _editorUiState.update { it.copy(scale = event.scale) }
            }
            is EditorUiEvents.OnOffsetChange -> {
                _editorUiState.update {
                    it.copy(offsetX = event.offsetX, offsetY = event.offsetY)
                }
            }
            is EditorUiEvents.OnShapeRadiusChange -> {
                _editorUiState.update { it.copy(shapeRadiusPercent = event.percent) }
            }
            is EditorUiEvents.OnClipHeightChange -> {
                _editorUiState.update { it.copy(clipHeightPercent = event.percent) }
            }
            is EditorUiEvents.OnHollowYChange -> {
                _editorUiState.update { it.copy(hollowCenterYPercent = event.percent.coerceIn(0f, 1f)) }
            }

            is EditorUiEvents.OnBgColorChange -> {
                _editorUiState.update { it.copy(bgColor = event.color) }
            }

            is EditorUiEvents.OnShapeChange -> {
                _editorUiState.update { it.copy(shape = event.shape) }
            }

            EditorUiEvents.OnControlPanelToggle -> {
                _editorUiState.update { it.copy(isControlPanelVisible = !it.isControlPanelVisible) }
            }

            is EditorUiEvents.OnColorPickerToggle -> {
                _editorUiState.update { it.copy(isColorPickerVisible = event.visible) }
            }

            is EditorUiEvents.OnSubjectToggle -> {
                _editorUiState.update { it.copy(isSubjectEnabled = event.enabled) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun processAndCacheImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _editorUiState.update { it.copy(isLoading = true, originalBitmap = null, error = null) }

            try {
                val bitmap = imageBytes.toImageBitmap()

                val originalFileName = "original_${Clock.System.now().toEpochMilliseconds()}.jpg"
                val cacheResult = saveService.saveToCache(imageBytes = imageBytes, fileName = originalFileName)

                cacheResult.onSuccess { uri ->
                    _editorUiState.update { it.copy(originalImageUri = uri, originalBitmap = bitmap) }
                }.onFailure { e ->
                    _editorUiState.update { it.copy(error = "Failed to cache original: ${e.message}") }
                }

                val result = backgroundRemover.removeBackground(imageBytes)

                result.fold(
                    onSuccess = { processedImage ->
                        val subjectFileName = "subject_${Clock.System.now().toEpochMilliseconds()}.png"

                        saveService.saveToCache(fileName = subjectFileName, imageBytes = processedImage.imageBytes)
                            .onSuccess { subjectUri ->
                                _editorUiState.update {
                                    it.copy(
                                        subjectImageUri = subjectUri,
                                        imageWidth = processedImage.width,
                                        imageHeight = processedImage.height,
                                        isLoading = false // Done!
                                    )
                                }
                            }
                            .onFailure { error ->
                                _editorUiState.update {
                                    it.copy(isLoading = false, error = "Failed to cache subject: ${error.message}")
                                }
                            }
                    },
                    onFailure = { error ->
                        _editorUiState.update {
                            it.copy(isLoading = false, error = "AI Processing failed: ${error.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                _editorUiState.update {
                    it.copy(isLoading = false, error = "Unexpected error: ${e.message}")
                }
            }
        }
    }




    override fun onCleared() {
        backgroundRemover.close()
        super.onCleared()
    }

}