package com.bhaskar.pixelwalls.data

import com.bhaskar.pixelwalls.domain.capture.ImageFormat
import com.bhaskar.pixelwalls.domain.capture.ImageSaveService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class PlatformImageSaveService : ImageSaveService {
    actual override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> = withContext(Dispatchers.IO) {
        val pictureDir = File(System.getProperty("user.home"), "Pictures/PixelWalls")
        pictureDir.mkdirs()

        val file = File(pictureDir, generateUniqueName(fileName, format.extension))
        file.writeBytes(imageBytes)

        Result.success(file.absolutePath)
    }

    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> {
        val cacheDir = File(System.getProperty("java.io.tmpdir"))
        val file = File(cacheDir, fileName)
        file.writeBytes(imageBytes)
        return Result.success(file.absolutePath)
    }

    actual override suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit> {
//        TODO("Not yet implemented")
        return Result.success(Unit)
    }

    private fun generateUniqueName(base: String, ext: String) = "${base}_${System.currentTimeMillis()}.$ext"
}