package com.bhaskar.pixelwalls.domain

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class PlatformWallpaperSetter(
    private val contextProvider: () -> Context
) : WallpaperSetter {
    actual override suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget
    ): WallpaperSetResult {

        return withContext(Dispatchers.IO) {

            val context = contextProvider()

            try {

                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: return@withContext WallpaperSetResult.Error("Failed to decode image")

                val wallpaperManager = WallpaperManager.getInstance(context)

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    val flag = when(target){
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

            } catch (e: SecurityException) {
                WallpaperSetResult.Error("Permission Denied")
            } catch (e: Exception) {
                WallpaperSetResult.Error("Unknown Error $e")
            }

        }

    }

    actual override fun canSetWallpaperDirectly(): Boolean = true

    actual override suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult {
        val context = contextProvider()

        return try {

            val tempFile = File(context.cacheDir, "wallpaper_temp.png")
            tempFile.writeBytes(imageBytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                setDataAndType(uri, "image/png")
                putExtra("mimeType", "image/png")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Set as Wallpaper"))
            WallpaperSetResult.Success
        } catch (e: Exception) {
            WallpaperSetResult.Error("Failed to open picker: ${e.message}")
        }

    }
}