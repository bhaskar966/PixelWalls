package com.bhaskar.pixelwalls.domain


sealed interface WallpaperSetResult {

    data object Success: WallpaperSetResult
    data class UserActionRequired(val instructions: String): WallpaperSetResult
    data class Error(val message: String): WallpaperSetResult

}

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

    fun canSetWallpaperDirectly(): Boolean

    suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult

}

expect class PlatformWallpaperSetter: WallpaperSetter {
    override suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget
    ): WallpaperSetResult

    override fun canSetWallpaperDirectly(): Boolean
    override suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult
}