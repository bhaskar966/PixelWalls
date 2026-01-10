package com.bhaskar.pixelwalls.data.file

import com.bhaskar.pixelwalls.domain.service.FileService
import com.bhaskar.pixelwalls.utils.PixelWallsPaths
import com.bhaskar.pixelwalls.utils.getPublicPicturesDir
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager

actual class PlatformFileService : FileService {

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun getSavedWallpaperPaths(): Result<List<String>> = withContext(Dispatchers.Default) {
        try {
            val baseDir = getPublicPicturesDir()
            val folderPath = "$baseDir/${PixelWallsPaths.FOLDER_NAME}"
            val files = NSFileManager.defaultManager.contentsOfDirectoryAtPath(folderPath, null)
                ?.filterIsInstance<String>()
                ?.filter { it.endsWith(".png") || it.endsWith(".jpg") || it.endsWith(".jpeg") }
                ?.map { "$folderPath/$it" }
                ?.reversed()
                ?: emptyList()

            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.Default) {
        val success = NSFileManager.defaultManager.removeItemAtPath(path, null)
        if (success) Result.success(Unit)
        else Result.failure(Exception("Failed to delete file"))
    }
}