package com.bhaskar.pixelwalls.backgroundremoval

import com.bhaskar.pixelwalls.domain.model.ProcessedImage

/**
 * Main background remover API
 */
interface BackgroundRemover {
    /**
     * Remove background from image bytes
     * @param imageBytes PNG or JPEG image bytes
     * @return Result with processed image or error
     */
    suspend fun removeBackground(imageBytes: ByteArray): Result<ProcessedImage>

    fun close()
}

/**
 * Create platform-specific BackgroundRemover instance
 */
expect fun createBackgroundRemover(): BackgroundRemover