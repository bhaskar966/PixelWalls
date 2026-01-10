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
    ): Result<String> = withContext(Dispatchers.IO) {
        val pictureDir = File(getPublicPicturesDir(), PixelWallsPaths.FOLDER_NAME)
        pictureDir.mkdirs()

        val file = File(pictureDir, generateUniqueName(fileName, format.extension))
        file.writeBytes(imageBytes)

        Result.success(file.absolutePath)
    }

    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(System.getProperty("java.io.tmpdir"))
            val file = File(cacheDir, fileName)
            file.writeBytes(imageBytes)

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
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val tempFile = File(System.getProperty("java.io.tmpdir"), "$fileName.png")
                tempFile.writeBytes(imageBytes)

                val os = System.getProperty("os.name").lowercase()
                if (os.contains("mac")) {
                    // Use the macOS 'open' command with the sharing service
                    // Note: This requires the file to exist on disk
                    ProcessBuilder("open", "-a", "Sharing", tempFile.absolutePath).start()
                } else {
                    // Fallback for Windows/Linux: Just open the file
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(tempFile)
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun generateUniqueName(base: String, ext: String) = "${base}_${System.currentTimeMillis()}.$ext"
}