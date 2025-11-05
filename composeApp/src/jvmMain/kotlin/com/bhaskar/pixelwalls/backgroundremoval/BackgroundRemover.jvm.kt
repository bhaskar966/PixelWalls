package com.bhaskar.pixelwalls.backgroundremoval

import ai.onnxruntime.*
import com.bhaskar.pixelwalls.domain.model.ProcessedImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.FloatBuffer
import javax.imageio.ImageIO
import kotlin.collections.forEach
import kotlin.math.exp

/**
 * Platform-specific factory function for Desktop
 * Creates a Desktop implementation using ONNX Runtime and U2-Net model
 */
actual fun createBackgroundRemover(): BackgroundRemover {
    return BackgroundRemoverDesktop()
}

/**
 * Desktop implementation of BackgroundRemover using ONNX Runtime
 *
 * Uses U2-Net (U2-netp) model for semantic segmentation to remove backgrounds.
 * The model is loaded from resources and runs inference on CPU.
 *
 * Output: Lossless PNG with transparency
 */
internal class BackgroundRemoverDesktop : BackgroundRemover {

    companion object {
        private const val MODEL_INPUT_SIZE = 320
        private const val MODEL_NAME = "u2netp.onnx"
        private const val NUM_THREADS = 4

        // ImageNet normalization parameters (required for U2-Net)
        private const val R_MEAN = 0.485f
        private const val G_MEAN = 0.456f
        private const val B_MEAN = 0.406f
        private const val R_STD = 0.229f
        private const val G_STD = 0.224f
        private const val B_STD = 0.225f

        // Mask generation parameters
        private const val ALPHA_THRESHOLD = 240  // Pixels above this become fully opaque
    }

    /**
     * ONNX Runtime environment (shared across sessions)
     */
    private val ortEnv: OrtEnvironment by lazy {
        OrtEnvironment.getEnvironment()
    }

    /**
     * ONNX inference session with U2-Net model
     * Lazily initialized on first use
     */
    private val session: OrtSession by lazy {
        val modelBytes = this::class.java.classLoader
            .getResourceAsStream(MODEL_NAME)
            ?.use { it.readBytes() }
            ?: throw ModelNotFoundException("Model file '$MODEL_NAME' not found in resources")

        val options = OrtSession.SessionOptions().apply {
            setIntraOpNumThreads(NUM_THREADS)
            setInterOpNumThreads(NUM_THREADS)
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        }

        ortEnv.createSession(modelBytes, options)
    }

    /**
     * Remove background from image using U2-Net semantic segmentation
     *
     * Process:
     * 1. Decode input image
     * 2. Resize to 320x320 and normalize with ImageNet parameters
     * 3. Run ONNX inference
     * 4. Apply sigmoid to output and generate mask
     * 5. Resize mask to original dimensions with bilinear interpolation
     * 6. Apply mask to create transparent background
     * 7. Encode as lossless PNG
     *
     * @param imageBytes Input image as byte array (JPEG, PNG, etc.)
     * @return Result containing processed image with transparent background
     */
    override suspend fun removeBackground(imageBytes: ByteArray): Result<ProcessedImage> =
        withContext(Dispatchers.IO) {
            runCatching {
                val startTime = System.currentTimeMillis()

                // Decode input image
                val originalImage = ByteArrayInputStream(imageBytes).use {
                    ImageIO.read(it)
                } ?: throw ImageDecodingException("Failed to decode image bytes")

                val width = originalImage.width
                val height = originalImage.height

                // Preprocess: resize and normalize with ImageNet parameters
                val inputTensor = createInputTensor(originalImage)

                // Run U2-Net inference
                val outputTensor = runInference(inputTensor)

                // Generate binary mask with sigmoid activation
                val mask = generateMask(outputTensor, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE)

                // Resize mask to original dimensions using bilinear interpolation
                val fullSizeMask = resizeMask(mask, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, width, height)

                // Apply mask to create transparent background
                val result = applyMask(originalImage, fullSizeMask)

                // Encode as lossless PNG
                val outputStream = ByteArrayOutputStream()
                ImageIO.write(result, "png", outputStream)
                val outputBytes = outputStream.toByteArray()

                val processingTime = System.currentTimeMillis() - startTime

                ProcessedImage(
                    imageBytes = outputBytes,
                    width = width,
                    height = height,
                    processingTimeMs = processingTime
                )
            }
        }

    /**
     * Preprocess image for U2-Net model
     *
     * - Resizes to 320x320
     * - Normalizes RGB values using ImageNet parameters
     * - Converts to CHW (Channel-Height-Width) tensor format
     *
     * @param image Original input image
     * @return FloatArray tensor ready for inference (1x3x320x320)
     */
    private fun createInputTensor(image: BufferedImage): FloatArray {
        // Resize to model input size
        val resized = BufferedImage(MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, BufferedImage.TYPE_INT_RGB)
        val graphics = resized.createGraphics()
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
        )
        graphics.drawImage(image, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, null)
        graphics.dispose()

        val tensor = FloatArray(MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3)

        // Extract pixels and normalize with ImageNet parameters
        for (y in 0 until MODEL_INPUT_SIZE) {
            for (x in 0 until MODEL_INPUT_SIZE) {
                val rgb = resized.getRGB(x, y)

                // Extract RGB components and normalize to 0-1
                val r = ((rgb shr 16) and 0xFF) / 255f
                val g = ((rgb shr 8) and 0xFF) / 255f
                val b = (rgb and 0xFF) / 255f

                val idx = y * MODEL_INPUT_SIZE + x

                // Apply ImageNet normalization and convert to CHW format
                tensor[idx] = (r - R_MEAN) / R_STD
                tensor[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE + idx] = (g - G_MEAN) / G_STD
                tensor[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 2 + idx] = (b - B_MEAN) / B_STD
            }
        }

        return tensor
    }

    /**
     * Generate binary mask from model output
     *
     * - Applies sigmoid function to convert raw output to probabilities
     * - Normalizes probabilities to 0-255 alpha values
     * - Forces high-confidence foreground pixels to full opacity to prevent dimming
     *
     * @param outputTensor Raw output from U2-Net model
     * @param width Mask width (320)
     * @param height Mask height (320)
     * @return ByteArray mask with alpha values (0 = transparent, 255 = opaque)
     */
    private fun generateMask(outputTensor: FloatArray, width: Int, height: Int): ByteArray {
        val mask = ByteArray(width * height)

        // Apply sigmoid to get probabilities (0.0 to 1.0)
        val sigmoidValues = FloatArray(outputTensor.size) { i ->
            sigmoid(outputTensor[i])
        }

        // Normalize sigmoid values to 0-255 range
        val min = sigmoidValues.minOrNull() ?: 0f
        val max = sigmoidValues.maxOrNull() ?: 1f
        val range = max - min

        // Handle flat masks (all same value)
        if (range < 0.0001f) {
            return ByteArray(sigmoidValues.size) {
                if (max > 0.5f) 255.toByte() else 0.toByte()
            }
        }

        // Convert probabilities to alpha values
        sigmoidValues.indices.forEach { i ->
            val normalizedAlpha = (((sigmoidValues[i] - min) / range) * 255f).toInt()

            // Force high-confidence foreground to full opacity (prevents dimming)
            // Preserve soft edges for semi-transparent pixels
            mask[i] = if (normalizedAlpha > ALPHA_THRESHOLD) {
                255.toByte()
            } else {
                normalizedAlpha.coerceIn(0, 255).toByte()
            }
        }

        return mask
    }

    /**
     * Sigmoid activation function
     * Converts unbounded float to probability in range (0, 1)
     */
    private fun sigmoid(x: Float): Float {
        return 1f / (1f + exp(-x))
    }

    /**
     * Apply alpha mask to original image to create transparent background
     *
     * @param original Original input image
     * @param mask Alpha mask (0 = transparent, 255 = opaque)
     * @return BufferedImage with transparent background
     */
    private fun applyMask(original: BufferedImage, mask: ByteArray): BufferedImage {
        val width = original.width
        val height = original.height

        val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                val maskAlpha = mask[idx].toInt() and 0xFF

                if (maskAlpha > 0) {
                    // Foreground: preserve RGB with mask alpha
                    val rgb = original.getRGB(x, y)
                    val r = (rgb shr 16) and 0xFF
                    val g = (rgb shr 8) and 0xFF
                    val b = rgb and 0xFF

                    val argb = (maskAlpha shl 24) or (r shl 16) or (g shl 8) or b
                    result.setRGB(x, y, argb)
                } else {
                    // Background: fully transparent
                    result.setRGB(x, y, 0x00000000)
                }
            }
        }

        return result
    }

    /**
     * Resize mask using bilinear interpolation for smooth edges
     *
     * Bilinear interpolation prevents pixelated edges by blending
     * the 4 nearest source pixels for each destination pixel.
     *
     * @param mask Source mask
     * @param oldW Source width
     * @param oldH Source height
     * @param newW Destination width
     * @param newH Destination height
     * @return Resized mask
     */
    private fun resizeMask(mask: ByteArray, oldW: Int, oldH: Int, newW: Int, newH: Int): ByteArray {
        val result = ByteArray(newW * newH)
        val xRatio = oldW.toFloat() / newW
        val yRatio = oldH.toFloat() / newH

        for (y in 0 until newH) {
            for (x in 0 until newW) {
                // Map destination coordinate to source
                val gx = x * xRatio
                val gy = y * yRatio

                val gxi = gx.toInt()
                val gyi = gy.toInt()

                // Get 4 nearest source pixels
                val c00 = mask.getOrNull(gyi * oldW + gxi)?.toUByte()?.toInt() ?: 0
                val c10 = mask.getOrNull(gyi * oldW + (gxi + 1))?.toUByte()?.toInt() ?: c00
                val c01 = mask.getOrNull((gyi + 1) * oldW + gxi)?.toUByte()?.toInt() ?: c00
                val c11 = mask.getOrNull((gyi + 1) * oldW + (gxi + 1))?.toUByte()?.toInt() ?: c00

                // Calculate interpolation weights
                val tx = gx - gxi
                val ty = gy - gyi

                // Bilinear interpolation
                val top = c00 * (1 - tx) + c10 * tx
                val bottom = c01 * (1 - tx) + c11 * tx
                val interpolatedValue = (top * (1 - ty) + bottom * ty).toInt()

                result[y * newW + x] = interpolatedValue.coerceIn(0, 255).toByte()
            }
        }
        return result
    }

    /**
     * Run ONNX inference with U2-Net model
     *
     * @param input Preprocessed input tensor (1x3x320x320)
     * @return Raw model output as FloatArray
     */
    private fun runInference(input: FloatArray): FloatArray {
        val inputTensor = OnnxTensor.createTensor(
            ortEnv,
            FloatBuffer.wrap(input),
            longArrayOf(1, 3, MODEL_INPUT_SIZE.toLong(), MODEL_INPUT_SIZE.toLong())
        )

        try {
            val inputName = session.inputNames.iterator().next()
            val results = session.run(mapOf(inputName to inputTensor))

            try {
                // Extract output tensor (shape: 1x1x320x320)
                val output = results[0].value as Array<Array<Array<FloatArray>>>
                return output[0][0].flatMap { it.toList() }.toFloatArray()
            } finally {
                results.close()
            }
        } finally {
            inputTensor.close()
        }
    }

    /**
     * Clean up ONNX resources
     */
    override fun close() {
        try {
            session.close()
        } catch (e: Exception) {
            // Session already closed, ignore
        }
    }
}