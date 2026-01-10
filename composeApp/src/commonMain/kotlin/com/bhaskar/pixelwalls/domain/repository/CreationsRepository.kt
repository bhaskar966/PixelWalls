package com.bhaskar.pixelwalls.domain.repository

import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget

interface CreationsRepository {

    suspend fun getSavedWallpaperPaths(): Result<List<String>>

    suspend fun deleteWallpaper(path: String): Result<Unit>

    suspend fun setWallpaper(path: String, target: WallpaperTarget? = null): WallpaperSetResult
    suspend fun shareWallpaper(path: String): Result<Unit>
    suspend fun openWallpaperPicker(path: String): WallpaperSetResult

    fun canApplyInDifferentScreens(): Boolean
    fun isShareSupported(): Boolean

}