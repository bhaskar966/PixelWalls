package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.pixelwalls.backgroundremoval.BackgroundRemover
import com.bhaskar.pixelwalls.domain.model.ProcessedImage
import com.bhaskar.pixelwalls.utils.cache.ImageCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.FileSystem
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EditorScreenViewModel(
    private val backgroundRemover: BackgroundRemover,
    private val imageCache: ImageCache
): ViewModel() {

    private val _editorUiState = MutableStateFlow(EditorState())
    val editorUiState = _editorUiState.asStateFlow()

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
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun processAndCacheImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _editorUiState.value = EditorState(isLoading = true)

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

//    @OptIn(ExperimentalTime::class)
//    private fun loadImage(imageUri: String) {
//
//        viewModelScope.launch {
//            _editorUiState.value = EditorState(isLoading = true)
//
//            try {
//
//                val imageBytes = fileSystem.read(imageUri.toPath()) {
//                    readByteArray()
//                }
//
//                val result = backgroundRemover.removeBackground(imageBytes)
//
//                result.fold(
//                    onSuccess = { processedImage ->
//
//                        val fileName = "processed_${Random.nextLong()}.png"
//                        val resultUri = imageCache.saveImageToCache(processedImage.imageBytes, fileName)
//
//                        _editorUiState.value = EditorState(
//                            originalImageUri = imageUri, // The full original image
//                            subjectImageUri = resultUri, // The foreground-only image
//                            subjectBounds = processedImage.toRelativeRect(), // Calculate the relative bounds
//                            isLoading = false
//                        )
//                    },
//                    onFailure = { error ->
//                        _editorUiState.value = EditorState(
//                            isLoading = false,
//                            error = "Background removal failed: ${error.message}"
//                        )
//                    }
//                )
//
//            } catch (e: Exception) {
//                _editorUiState.value = EditorState(
//                    isLoading = false,
//                    error = "Failed to load image: ${e.message}"
//                )
//            }
//
//        }
//
//    }

    private fun ProcessedImage.toRelativeRect(): Rect {

        val originalWidth = this.width.toFloat()
        val originalHeight = this.height.toFloat()

        val foregroundWidth = this.width * 0.5f
        val foregroundHeight = this.height * 0.7f

        val left = (originalWidth - foregroundWidth) / 2
        val top = (originalHeight - foregroundHeight) / 2
        val right = left + foregroundWidth
        val bottom = top + foregroundHeight

        return Rect(
            left = left / originalWidth,
            top = top / originalHeight,
            right = right / originalWidth,
            bottom = bottom / originalHeight
        )

    }

//    suspend fun processImage(imageBytes: ByteArray) {
//        _editorUiState.value = EditorState(isLoading = true)
//        val result = backgroundRemover.removeBackground(imageBytes)
//
//        result.fold(
//            onSuccess = { processedImage ->
//                _editorUiState.value = EditorState(
//                    originalImageUri = imageBytes.toImageBitmap(),
//                    subjectImageUri = processedImage.imageBytes.toImageBitmap(),
//                    subjectBounds = processedImage.toRelativeRect(),
//                    isLoading = false
//                )
//            },
//            onFailure = { error ->
//                _editorUiState.value = EditorState(
//                    isLoading = false,
//                    error = "Background removal failed: ${error.message}"
//                )
//            }
//        )
//    }

    override fun onCleared() {
        backgroundRemover.close()
        super.onCleared()
    }

}