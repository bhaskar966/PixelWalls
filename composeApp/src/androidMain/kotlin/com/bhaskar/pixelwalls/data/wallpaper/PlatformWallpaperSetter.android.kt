package com.bhaskar.pixelwalls.data.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.content.FileProvider
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.WallpaperSetter
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class PlatformWallpaperSetter(
    private val contextProvider: () -> Context
) : WallpaperSetter {

    actual override val canApplyWallpaperInDifferentScreens: Boolean = true

    actual override suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget
    ): WallpaperSetResult {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return performSetWallpaper(bitmap, target)
    }

    actual override suspend fun setWallpaper(
        filePath: String,
        target: WallpaperTarget
    ): WallpaperSetResult {
        val bitmap = BitmapFactory.decodeFile(filePath)
        return performSetWallpaper(bitmap, target)
    }

    private suspend fun performSetWallpaper(bitmap: Bitmap?, target: WallpaperTarget): WallpaperSetResult {
        return withContext(Dispatchers.IO) {
            val context = contextProvider()
            try {
                if (bitmap == null) return@withContext WallpaperSetResult.Error("Failed to decode image")
                val wallpaperManager = WallpaperManager.getInstance(context)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val flag = when (target) {
                        WallpaperTarget.HOME_SCREEN -> WallpaperManager.FLAG_SYSTEM
                        WallpaperTarget.LOCK_SCREEN -> WallpaperManager.FLAG_LOCK
                        WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }
                    wallpaperManager.setBitmap(bitmap, null, true, flag)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
                bitmap.recycle()
                WallpaperSetResult.Success
            } catch (e: Exception) {
                WallpaperSetResult.Error(e.message ?: "Unknown Error")
            }
        }
    }

    actual override fun canSetWallpaperDirectly(): Boolean = true

    actual override suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult {
        val file = imageBytes.saveToTempFile()
        return openPickerInternal(file)
    }

    actual override suspend fun openWallpaperPicker(path: String): WallpaperSetResult {
        val file = File(path)
        return openPickerInternal(file)
    }

    private fun openPickerInternal(file: File): WallpaperSetResult {
        val context = contextProvider()
        return try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                setDataAndType(uri, "image/png")
                putExtra("mimeType", "image/png")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Set as Wallpaper"))
            WallpaperSetResult.Success
        } catch (e: Exception) {
            WallpaperSetResult.Error(e.message ?: "Failed to open picker")
        }
    }

    private fun ByteArray.saveToTempFile(): File {
        val context = contextProvider()
        val file = File(context.cacheDir, "wallpaper_temp.png")
        file.writeBytes(this)
        return file
    }
}