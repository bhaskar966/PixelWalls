package com.bhaskar.pixelwalls.data.repository

import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.bhaskar.pixelwalls.domain.model.ProcessedImage
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.repository.EditorRepository
import com.bhaskar.pixelwalls.domain.service.ImageCaptureService
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.domain.service.ModelStatus
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import com.bhaskar.pixelwalls.domain.service.WallpaperSetter
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover
import kotlinx.coroutines.flow.StateFlow

class EditorRepositoryImpl(
    private val backgroundRemover: BackgroundRemover,
    private val saveService: ImageSaveService,
    private val wallpaperSetter: WallpaperSetter,
    private val modelStatusService: ModelStatusService,
): EditorRepository {

    override suspend fun processImage(imageBytes: ByteArray): Result<ProcessedImage> {
        return backgroundRemover.removeBackground(imageBytes)
    }

    override fun closeBackgroundRemoverSession() {
        backgroundRemover.close()
    }

    override suspend fun cacheImage(
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        return saveService.saveToCache(fileName, bytes)
    }

    override suspend fun saveToGallery(
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        return saveService.saveToGallery(fileName, bytes)
    }

    override suspend fun setWallpaper(
        bytes: ByteArray,
        target: WallpaperTarget?
    ): WallpaperSetResult {
        return wallpaperSetter.setWallpaper(
            imageBytes = bytes,
            target = target ?: WallpaperTarget.HOME_SCREEN
        )
    }

    override suspend fun shareImage(
        fileName: String,
        bytes: ByteArray
    ): Result<Unit> {
        return saveService.shareImage(fileName, bytes)
    }

    override suspend fun openWallpaperPicker(bytes: ByteArray): WallpaperSetResult {
        return wallpaperSetter.openWallpaperPicker(bytes)
    }

    override fun canSetWallpaperDirectly(): Boolean = wallpaperSetter.canApplyWallpaperInDifferentScreens

    override fun canApplyWallpaperInDifferentScreens(): Boolean = wallpaperSetter.canApplyWallpaperInDifferentScreens

    override fun isShareSupported(): Boolean = saveService.isShareSupported

    override val modelStatus: StateFlow<ModelStatus> = modelStatusService.status

    override fun checkModelStatus() = modelStatusService.checkStatus()

    override fun downloadModel() = modelStatusService.downloadModels()

}