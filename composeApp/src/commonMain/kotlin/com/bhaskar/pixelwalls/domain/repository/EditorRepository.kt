package com.bhaskar.pixelwalls.domain.repository

import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.bhaskar.pixelwalls.domain.model.ProcessedImage
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.ModelStatus
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import kotlinx.coroutines.flow.StateFlow

interface EditorRepository {

    suspend fun processImage(imageBytes: ByteArray): Result<ProcessedImage>
    fun closeBackgroundRemoverSession()
    suspend fun cacheImage(fileName: String, bytes: ByteArray): Result<String>
    suspend fun saveToGallery(fileName: String, bytes: ByteArray): Result<String>
    suspend fun setWallpaper(bytes: ByteArray, target: WallpaperTarget? = null): WallpaperSetResult
    suspend fun shareImage(fileName: String, bytes: ByteArray): Result<Unit>
    suspend fun openWallpaperPicker(bytes: ByteArray): WallpaperSetResult

    fun canSetWallpaperDirectly(): Boolean
    fun canApplyWallpaperInDifferentScreens(): Boolean
    fun isShareSupported(): Boolean

    val modelStatus: StateFlow<ModelStatus>
    fun checkModelStatus()
    fun downloadModel()
}