package com.bhaskar.pixelwalls.utils.cache

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class PlatformImageCache : ImageCache {
    actual override suspend fun saveImageToCache(
        bytes: ByteArray,
        fileName: String
    ): String {
        return withContext(Dispatchers.IO) {
            val tempDir = System.getProperty("java.io.tmpdir")
            val file = File(tempDir, fileName)
            file.writeBytes(bytes)
            val path = file.absolutePath.replace("\\", "/")
            if (path.startsWith("/")) {
                // Mac/Linux
                "file://$path"
            } else {
                // Windows
                "file:///$path"
            }

        }
    }
}