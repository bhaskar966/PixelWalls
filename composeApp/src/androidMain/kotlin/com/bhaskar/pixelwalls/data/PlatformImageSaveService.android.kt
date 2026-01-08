package com.bhaskar.pixelwalls.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.bhaskar.pixelwalls.domain.capture.ImageFormat
import com.bhaskar.pixelwalls.domain.capture.ImageSaveService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import kotlin.random.Random

actual class PlatformImageSaveService(
    private val contextProvider: () -> Context
) : ImageSaveService {
    actual override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> = withContext(Dispatchers.IO) {

        val context = contextProvider()
        val finalName = generateUniqueName(fileName, format.extension)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(context, finalName, imageBytes, format)
        } else {
            saveToLegacyStorage(context, finalName, imageBytes, format)
        }

    }

    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> {
        return withContext(Dispatchers.IO){
            val context = contextProvider()
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeBytes(imageBytes)
            Result.success(cacheFile.absolutePath)
        }
    }

    actual override suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val context = contextProvider()
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeBytes(imageBytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share Wallpaper"))
            Result.success(Unit)
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
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PixelWalls")
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

        val pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val pixelWallsDir = File(pictureDir, "PixelWalls")
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