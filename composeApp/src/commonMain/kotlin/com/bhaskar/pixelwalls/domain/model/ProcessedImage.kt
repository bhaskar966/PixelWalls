package com.bhaskar.pixelwalls.domain.model

/**
 * Result of background removal processing
 * Contains raw image bytes (format depends on platform)
 */
data class ProcessedImage(
    /**
     * Raw image bytes
     * - Android: Can be converted to Bitmap
     * - Desktop: PNG bytes
     * - iOS: UIImage bytes
     */
    val imageBytes: ByteArray,

    val width: Int,
    val height: Int,
    val processingTimeMs: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessedImage) return false

        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (processingTimeMs != other.processingTimeMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageBytes.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + processingTimeMs.hashCode()
        return result
    }
}
