package com.bhaskar.pixelwalls.domain.model

sealed interface WallpaperSetResult {

    data object Success: WallpaperSetResult
    data class UserActionRequired(val instructions: String): WallpaperSetResult
    data class Error(val message: String): WallpaperSetResult

}