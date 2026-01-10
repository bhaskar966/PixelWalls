package com.bhaskar.pixelwalls.data.save

import com.bhaskar.pixelwalls.domain.service.ImageFormat
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.utils.PixelWallsPaths
import com.bhaskar.pixelwalls.utils.getPublicPicturesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

actual class PlatformImageSaveService : ImageSaveService {

    actual override val isShareSupported: Boolean = false

    actual override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> = performSaveToGallery(
        fileName = fileName,
        bytes = imageBytes,
        format = format
    )

    actual override suspend fun saveToGallery(
        fileName: String,
        filePath: String,
        format: ImageFormat
    ): Result<String> = performSaveToGallery(
        fileName = fileName,
        bytes = File(filePath).readBytes(),
        format = format
    )

    private suspend fun performSaveToGallery(
        fileName: String,
        bytes: ByteArray,
        format: ImageFormat
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val pictureDir = File(getPublicPicturesDir(), PixelWallsPaths.FOLDER_NAME)
            if (!pictureDir.exists()) pictureDir.mkdirs()

            val finalName = "${fileName}_${System.currentTimeMillis()}.${format.extension}"
            val file = File(pictureDir, finalName)
            file.writeBytes(bytes)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> = performSaveToCache(
        fileName = fileName,
        bytes = imageBytes
    )

    actual override suspend fun saveToCache(
        fileName: String,
        filePath: String
    ): Result<String> = performSaveToCache(
        fileName = fileName,
        bytes = File(filePath).readBytes()
    )

    private suspend fun performSaveToCache(
        fileName: String,
        bytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(System.getProperty("java.io.tmpdir"))
            val file = File(cacheDir, fileName)
            file.writeBytes(bytes)

            val path = file.absolutePath.replace("\\", "/")
            val uri = if (path.startsWith("/")) "file://$path" else "file:///$path"
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    actual override suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit> = performShare(bytes = imageBytes, fileName =  fileName)

    actual override suspend fun shareImage(
        fileName: String,
        filePath: String
    ): Result<Unit> = performShare(
        bytes = File(filePath).readBytes(),
        fileName = fileName
    )

    private suspend fun performShare(bytes: ByteArray, fileName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(System.getProperty("java.io.tmpdir"), "$fileName.png")
            tempFile.writeBytes(bytes)
            val os = System.getProperty("os.name").lowercase()
            if (os.contains("mac")) {
                ProcessBuilder("open", "-a", "Sharing", tempFile.absolutePath).start()
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateUniqueName(base: String, ext: String) = "${base}_${System.currentTimeMillis()}.$ext"
}