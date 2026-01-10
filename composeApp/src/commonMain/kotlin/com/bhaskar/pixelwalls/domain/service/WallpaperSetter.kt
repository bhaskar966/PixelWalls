package com.bhaskar.pixelwalls.domain.service

import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult

enum class WallpaperTarget {
    HOME_SCREEN,
    LOCK_SCREEN,
    BOTH
}


interface WallpaperSetter {
    suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget = WallpaperTarget.HOME_SCREEN
    ): WallpaperSetResult

    suspend fun setWallpaper(
        filePath: String,
        target: WallpaperTarget = WallpaperTarget.HOME_SCREEN
    ): WallpaperSetResult

    fun canSetWallpaperDirectly(): Boolean

    suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult
    suspend fun openWallpaperPicker(path: String): WallpaperSetResult

    val canApplyWallpaperInDifferentScreens: Boolean
}