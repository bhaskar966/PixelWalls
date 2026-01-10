package com.bhaskar.pixelwalls.data.repository

import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.repository.CreationsRepository
import com.bhaskar.pixelwalls.domain.service.FileService
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.domain.service.WallpaperSetter
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget

class CreationsRepositoryImpl(
    private val fileService: FileService,
    private val wallpaperSetter: WallpaperSetter,
    private val saveService: ImageSaveService
): CreationsRepository {

    override suspend fun getSavedWallpaperPaths(): Result<List<String>> {
        return fileService.getSavedWallpaperPaths()
    }

    override suspend fun deleteWallpaper(path: String): Result<Unit> {
        return fileService.deleteFile(path).also { result ->
            result.onFailure { error ->
                println("CreationsRepository" + " Delete failed: ${error.message}")
            }
        }
    }

    override suspend fun setWallpaper(
        path: String,
        target: WallpaperTarget?
    ): WallpaperSetResult {
        return wallpaperSetter.setWallpaper(
            filePath = path,
            target = target ?: WallpaperTarget.HOME_SCREEN
        )
    }

    override suspend fun shareWallpaper(path: String): Result<Unit> {
        return saveService.shareImage(
            fileName = "SharedWall",
            filePath = path
        )
    }

    override suspend fun openWallpaperPicker(path: String): WallpaperSetResult {
        return wallpaperSetter.openWallpaperPicker(
            path = path
        )
    }

    override fun canApplyInDifferentScreens(): Boolean = wallpaperSetter.canApplyWallpaperInDifferentScreens

    override fun isShareSupported(): Boolean = saveService.isShareSupported
}