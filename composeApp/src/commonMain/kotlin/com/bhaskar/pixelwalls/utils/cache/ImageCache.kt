package com.bhaskar.pixelwalls.utils.cache

/**
 * An interface to handle caching image data on different platforms.
 */
interface ImageCache {
    /**
     * Saves image bytes to a temporary cache file and returns its readable path.
     * @param bytes The ByteArray of the image.
     * @param fileName A unique name for the file (e.g., using timestamp).
     * @return The absolute, readable path to the newly created cache file.
     */
    suspend fun saveImageToCache(bytes: ByteArray, fileName: String): String

}

/**
 * Creates a platform-specific instance of the ImageCache.
 */
expect class PlatformImageCache: ImageCache {
    override suspend fun saveImageToCache(
        bytes: ByteArray,
        fileName: String
    ): String
}