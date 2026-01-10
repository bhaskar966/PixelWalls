package com.bhaskar.pixelwalls.data.save

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.bhaskar.pixelwalls.domain.service.ImageFormat
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.utils.PixelWallsPaths
import com.bhaskar.pixelwalls.utils.getPublicPicturesDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import kotlin.random.Random

actual class PlatformImageSaveService(
    private val contextProvider: () -> Context
) : ImageSaveService {

    actual override val isShareSupported: Boolean = true

    actual override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> = withContext(Dispatchers.IO) {
        performSaveToGallery(fileName, imageBytes, format)
    }

    actual override suspend fun saveToGallery(
        fileName: String,
        filePath: String,
        format: ImageFormat
    ): Result<String> = withContext(Dispatchers.IO) {
        val context = contextProvider()
        try {
            val bytes = if (filePath.startsWith("content://")) {
                context
                    .contentResolver
                    .openInputStream(filePath.toUri())
                    ?.use { it.readBytes() }
                    ?: throw Exception("Could not read stream")
            } else {
                File(filePath).readBytes()
            }
            performSaveToGallery(fileName, bytes, format)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun performSaveToGallery(
        fileName: String,
        bytes: ByteArray,
        format: ImageFormat
    ): Result<String> {
        val context = contextProvider()
        val finalName = generateUniqueName(fileName, format.extension)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(context, finalName, bytes, format)
        } else {
            saveToLegacyStorage(context, finalName, bytes, format)
        }
    }

    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        performSaveToCache(fileName, imageBytes)
    }

    actual override suspend fun saveToCache(
        fileName: String,
        filePath: String
    ): Result<String> = withContext(Dispatchers.IO) {
        performSaveToCache(fileName, File(filePath).readBytes())
    }

    private fun performSaveToCache(
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        return try {
            val context = contextProvider()
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeBytes(bytes)
            Result.success(cacheFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cachedPath = saveToCache(fileName, imageBytes).getOrNull()
        performShare(cachedPath)
    }

    actual override suspend fun shareImage(
        fileName: String,
        filePath: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        performShare(filePath)
    }


    private fun performShare(path: String?): Result<Unit> {
        if (path == null) return Result.failure(Exception("Path is null"))
        val context = contextProvider()

        return try {
            val uri = if (path.startsWith("content://")) {
                path.toUri()
            } else {
                val file = File(path)
                if (!file.exists()) return Result.failure(Exception("File not found"))
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Wallpaper").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    private fun saveToMediaStore(
        context: Context,
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> {
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/${PixelWallsPaths.FOLDER_NAME}")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        return try {

            val uri = context.contentResolver.insert(collection, values)
                ?: return Result.failure(Exception("Failed to create MediaStore entry."))

            context.contentResolver.openOutputStream(uri).use { stream ->
                BufferedOutputStream(stream, 8192).use { it.write(imageBytes) }
            }

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            Result.success(uri.toString())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveToLegacyStorage(
        context: Context,
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> {

        val pictureDir = File(getPublicPicturesDir())
        val pixelWallsDir = File(pictureDir, PixelWallsPaths.FOLDER_NAME)
        pixelWallsDir.mkdirs()

        val file = File(pixelWallsDir, fileName)
        file.writeBytes(imageBytes)

        MediaScannerConnection.scanFile(
            context, arrayOf(file.absolutePath), arrayOf(format.mimeType), null
        )

        return Result.success(file.absolutePath)

    }


    private fun generateUniqueName(baseName: String, ext: String): String {
        val timeStamp = System.currentTimeMillis()
        val rand = Random.nextInt(1000,9999)
        return "${baseName}_${timeStamp}_$rand.$ext"
    }
}