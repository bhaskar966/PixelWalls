package com.bhaskar.pixelwalls.utils.cache

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class PlatformImageCache(
    private val context: Context
) : ImageCache {
    actual override suspend fun saveImageToCache(
        bytes: ByteArray,
        fileName: String
    ): String {
        return withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, fileName)
            file.writeBytes(bytes)
            file.absolutePath
        }
    }
}