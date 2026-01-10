package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.repository.EditorRepository
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.domain.service.ModelStatus
import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.ActionStep
import com.bhaskar.pixelwalls.utils.editor.toImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class EditorScreenViewModel(
    private val repository: EditorRepository
): ViewModel() {

    private val _editorUiState = MutableStateFlow(EditorState())
    val editorUiState = _editorUiState.asStateFlow()

    init {

        viewModelScope.launch {
            repository.modelStatus.collect { status ->
                _editorUiState.update { it.copy(modelStatus = status) }
            }
        }

        repository.checkModelStatus()

        _editorUiState.update {
            it.copy(
                canSetWallpaperDirectly = repository.canSetWallpaperDirectly(),
                canApplyInDifferentScreens = repository.canApplyWallpaperInDifferentScreens(),
                isShareSupported = repository.isShareSupported()
            )
        }
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

            is EditorUiEvents.OnDismissDialog -> {
                _editorUiState.update { it.copy(
                    showWallpaperDialog = false,
                    currentActionStep = ActionStep.Main,
                    wallpaperResult = null
                ) }
            }

            is EditorUiEvents.OnSaveToGalleryClick -> saveToGallery()
            is EditorUiEvents.OnSetWallpaperClick -> setWallpaper(event.target)
            is EditorUiEvents.OnShareClick -> shareImage()
            is EditorUiEvents.OnLocateWallpaperClick -> locateWallpaper()
            is EditorUiEvents.OnCaptured -> {
                _editorUiState.update { it.copy(
                    capturedBytes = event.bytes,
                    showWallpaperDialog = true
                ) }
            }

            is EditorUiEvents.OnDownloadModel -> downloadModel()

            is EditorUiEvents.OnControlPageChange -> {
                _editorUiState.update { it.copy(currentControlPage = event.page) }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun processAndCacheImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _editorUiState.update { it.copy(isLoading = true, originalBitmap = null, error = null) }

            try {
                val bitmap = imageBytes.toImageBitmap()
                val timestamp = Clock.System.now().toEpochMilliseconds()

                repository.cacheImage("original_$timestamp.jpg", imageBytes).onSuccess { uri ->
                    _editorUiState.update { it.copy(originalImageUri = uri, originalBitmap = bitmap) }
                }

                repository.processImage(imageBytes).fold(
                    onSuccess = { processed ->
                        repository.cacheImage("subject_$timestamp.png", processed.imageBytes).onSuccess { uri ->
                            _editorUiState.update {
                                it.copy(
                                    subjectImageUri = uri,
                                    imageWidth = processed.width,
                                    imageHeight = processed.height,
                                    isLoading = false
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        _editorUiState.update { it.copy(isLoading = false, error = error.message) }
                    }
                )
            } catch (e: Exception) {
                _editorUiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }


    private fun setWallpaper(target: WallpaperTarget?) {
        val bytes = _editorUiState.value.capturedBytes ?: return

        // Show target picker if platform supports it and no target was specified
        if (target == null && _editorUiState.value.canApplyInDifferentScreens) {
            _editorUiState.update {
                it.copy(currentActionStep = ActionStep.TargetSelection)
            }
            return
        }

        viewModelScope.launch {
            _editorUiState.update { it.copy(isOperating = true) }
            val result = repository.setWallpaper(bytes, target)
            _editorUiState.update {
                it.copy(
                    isOperating = false,
                    wallpaperResult = result,
                    currentActionStep = ActionStep.Result
                )
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun saveToGallery() {
        val bytes = _editorUiState.value.capturedBytes ?: return
        val fileName = "PixelWall_${Clock.System.now().toEpochMilliseconds()}.png"
        viewModelScope.launch {
            _editorUiState.update { it.copy(isOperating = true) }
            repository.saveToGallery(fileName, bytes)
                .onSuccess { path ->
                    _editorUiState.update {
                        it.copy(
                            isOperating = false,
                            currentActionStep = ActionStep.Result,
                            wallpaperResult = WallpaperSetResult.Success
                        )
                    }
                }
                .onFailure { error ->
                    _editorUiState.update {
                        it.copy(
                            isOperating = false,
                            currentActionStep = ActionStep.Result,
                            wallpaperResult = WallpaperSetResult.Error(error.message ?: "Save failed")
                        )
                    }
                }
        }
    }

    private fun shareImage() {
        val bytes = editorUiState.value.capturedBytes ?: return
        viewModelScope.launch {
            _editorUiState.update { it.copy(isOperating = true) }
            repository.shareImage("PixelWall_Share", bytes)
            _editorUiState.update { it.copy(isOperating = false) }
            onEvent(EditorUiEvents.OnDismissDialog)
        }
    }

    private fun locateWallpaper() {
        val bytes = editorUiState.value.capturedBytes ?: return
        viewModelScope.launch {
            repository.openWallpaperPicker(bytes)
        }
    }

    private fun downloadModel() {
        viewModelScope.launch {
            repository.downloadModel()
        }
    }


    override fun onCleared() {
        repository.closeBackgroundRemoverSession()
        super.onCleared()
    }

}