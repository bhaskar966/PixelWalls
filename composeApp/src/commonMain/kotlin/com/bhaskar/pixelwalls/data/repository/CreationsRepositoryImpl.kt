package com.bhaskar.pixelwalls.data.repository

import com.bhaskar.pixelwalls.domain.repository.CreationsRepository
import com.bhaskar.pixelwalls.domain.service.FileService

class CreationsRepositoryImpl(
    private val fileService: FileService
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
}