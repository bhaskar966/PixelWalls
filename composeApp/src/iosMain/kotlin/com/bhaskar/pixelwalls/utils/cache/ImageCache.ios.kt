package com.bhaskar.pixelwalls.utils.cache

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class PlatformImageCache : ImageCache {
    actual override suspend fun saveImageToCache(
        bytes: ByteArray,
        fileName: String
    ): String {
        val cacheDir = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
        true
        ).firstOrNull() as? String ?: throw IllegalStateException("Cant not find cache directory")

        return "$cacheDir/$fileName"
    }
}