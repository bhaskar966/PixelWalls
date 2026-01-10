package com.bhaskar.pixelwalls.domain.repository

interface CreationsRepository {

    suspend fun getSavedWallpaperPaths(): Result<List<String>>

    suspend fun deleteWallpaper(path: String): Result<Unit>

}