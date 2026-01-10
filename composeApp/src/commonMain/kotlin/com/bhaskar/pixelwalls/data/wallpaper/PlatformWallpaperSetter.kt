package com.bhaskar.pixelwalls.data.wallpaper

import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.WallpaperSetter
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget

expect class PlatformWallpaperSetter: WallpaperSetter {
    override suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget
    ): WallpaperSetResult

    override suspend fun setWallpaper(
        filePath: String,
        target: WallpaperTarget
    ): WallpaperSetResult

    override fun canSetWallpaperDirectly(): Boolean
    override suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult
    override suspend fun openWallpaperPicker(path: String): WallpaperSetResult
    override val canApplyWallpaperInDifferentScreens: Boolean
}