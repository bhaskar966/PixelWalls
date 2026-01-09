package com.bhaskar.pixelwalls.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import com.bhaskar.pixelwalls.utils.cache.ImageCache
import com.bhaskar.pixelwalls.utils.editor.toImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EditorScreenViewModel(
    private val backgroundRemover: BackgroundRemover,
    private val imageCache: ImageCache,
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
            _editorUiState.value = EditorState(isLoading = true, originalBitmap = null)

            try {
                // 1. Cache the original image and get a readable path.
                val originalFileName = "original_${Clock.System.now().toEpochMilliseconds()}.jpg"
                val originalPath = imageCache.saveImageToCache(imageBytes, originalFileName)

                // 2. Process the image.
                val result = backgroundRemover.removeBackground(imageBytes)

                result.fold(
                    onSuccess = { processedImage ->
                        // 3. Cache the processed subject image and get another readable path.
                        val subjectFileName = "subject_${Clock.System.now().toEpochMilliseconds()}.png"
                        val subjectPath = imageCache.saveImageToCache(processedImage.imageBytes, subjectFileName)

                        // 4. Update the state with the NEW, RELIABLE file paths.
                        _editorUiState.value = EditorState(
                            originalImageUri = originalPath,
                            originalBitmap = imageBytes.toImageBitmap(),
                            subjectImageUri = subjectPath,
                            imageWidth = processedImage.width,
                            imageHeight = processedImage.height,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _editorUiState.value = EditorState(
                            isLoading = false,
                            error = "Background removal failed: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                // This can catch errors from the caching step
                _editorUiState.value = EditorState(
                    isLoading = false,
                    error = "Failed to cache image: ${e.message}"
                )
            }
        }
    }



    override fun onCleared() {
        backgroundRemover.close()
        super.onCleared()
    }

}