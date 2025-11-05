package com.bhaskar.pixelwalls.backgroundremoval

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.exp


/**
 * Shared image processing utilities used across all platforms
 */
internal object ImageUtils {

    // ImageNet normalization constants (standard for U2-Net model)
    private val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val STD = floatArrayOf(0.229f, 0.224f, 0.225f)

    /**
     * Convert ARGB pixel array to normalized CHW float array for ONNX input
     *
     * @param pixels IntArray of ARGB pixels
     * @param width Image width
     * @param height Image height
     * @return FloatArray in CHW format (Channels x Height x Width)
     */
    fun normalizeImageData(pixels: IntArray, width: Int, height: Int): FloatArray {
        Logger.debug("🔍 Image normalization:")
        Logger.debug("  Input size: ${pixels.size}")
        Logger.debug("  Dimensions: ${width}x${height}")

        val size = width * height
        val result = FloatArray(size * 3) // CHW format: channels * height * width

        for (i in pixels.indices) {
            val pixel = pixels[i]

            // Extract RGB (ARGB format)
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            // Store in CHW format: [RRR...GGG...BBB...]
            result[i] = r                    // Red channel
            result[size + i] = g            // Green channel
            result[size * 2 + i] = b        // Blue channel
        }

        // Debug: Check first few values
        Logger.debug("  First pixel RGB: [${result[0]}, ${result[size]}, ${result[size*2]}]")
        Logger.debug("  Value range: ${result.minOrNull()} to ${result.maxOrNull()}")

        return result
    }

    /**
     * Normalize ONNX mask output (float values) to byte range (0-255)
     *
     * @param maskData Raw float array from ONNX model
     * @return ByteArray with values normalized to 0-255
     */
    fun normalizeMask(maskData: FloatArray): ByteArray {
        if (maskData.isEmpty()) {
            throw InferenceException("Empty mask data")
        }

        // Find min and max for normalization
        val min = maskData.minOrNull() ?: 0f
        val max = maskData.maxOrNull() ?: 1f

        Logger.debug("🔍 Mask normalization:")
        Logger.debug("  Input size: ${maskData.size}")
        Logger.debug("  Min: $min, Max: $max")

        // If all values are the same, return all zeros or all 255
        if (max - min < 0.0001f) {
            Logger.debug("  ⚠️ Flat mask detected (all same value)")
            return ByteArray(maskData.size) { if (max > 0.5f) 255.toByte() else 0.toByte() }
        }

        // Normalize to 0-255 range
        val normalized = ByteArray(maskData.size) { i ->
            val normalized = ((maskData[i] - min) / (max - min) * 255f).toInt()
            normalized.coerceIn(0, 255).toByte()
        }

        // Debug output stats
        val outputMin = normalized.minOrNull()?.toInt()?.and(0xFF) ?: 0
        val outputMax = normalized.maxOrNull()?.toInt()?.and(0xFF) ?: 0
        val outputAvg = normalized.map { it.toInt() and 0xFF }.average()

        Logger.debug("  Output - Min: $outputMin, Max: $outputMax, Avg: $outputAvg")

        return normalized
    }

    /**
     * Resize mask from source dimensions to target dimensions
     * Uses nearest-neighbor interpolation for speed
     *
     * @param mask Source mask as ByteArray
     * @param srcWidth Source width
     * @param srcHeight Source height
     * @param dstWidth Target width
     * @param dstHeight Target height
     * @return Resized mask
     */

    fun resizeMask(
        mask: ByteArray,
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int
    ): ByteArray {
        val result = ByteArray(dstHeight * dstWidth)

        for (y in 0 until dstHeight){
            for (x in 0 until dstWidth){
                // Map destination coordinates to source coordinates
                val srcX = (x * srcWidth) / dstWidth
                val srcY = (y * srcHeight) / dstHeight

                // Nearest neighbor sampling
                val srcIdx = srcY * srcWidth + srcX
                val dstIdx = y * dstWidth + x

                result[dstIdx] = mask[srcIdx]
            }
        }

        return result
    }

    /**
     * Apply simple edge smoothing to reduce jagged edges
     *
     * @param alpha Alpha value (0-255)
     * @return Smoothed alpha value
     */
    fun smoothAlpha(alpha: Int): Int {
        if(alpha == 0 || alpha == 255) return alpha

        // Sigmoid-like smoothing for semi-transparent pixels
        val normalized = alpha / 255.0
        val smoothed = 1.0 / (1.0 + exp(-12.0 * (normalized - 0.5)))
        return (smoothed * 255).toInt().coerceIn(0, 255)
    }
}

expect fun ByteArray.toImageBitmap(): ImageBitmap