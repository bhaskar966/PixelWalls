package com.bhaskar.pixelwalls.domain.service

interface FileService {

    suspend fun getSavedWallpaperPaths(): Result<List<String>>

    suspend fun deleteFile(path: String): Result<Unit>

}