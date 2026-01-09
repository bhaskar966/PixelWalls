package com.bhaskar.pixelwalls.data.background

import com.bhaskar.pixelwalls.domain.service.background.BackgroundRemover
import com.bhaskar.pixelwalls.domain.model.ProcessedImage
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap
import com.bhaskar.pixelwalls.domain.service.background.ImageDecodingException
import com.bhaskar.pixelwalls.domain.service.background.InferenceException
import com.bhaskar.pixelwalls.domain.service.background.Logger
import java.nio.FloatBuffer


actual fun createBackgroundRemover(): BackgroundRemover {
    return BackgroundRemoverAndroid()
}

internal class BackgroundRemoverAndroid() : BackgroundRemover {

    // MLKit Subject Segmenter (lazy initialization)
    private val segmenter by lazy {
        val options = SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask() // Get confidence mask
            .build()

        SubjectSegmentation.getClient(options)
    }

    override suspend fun removeBackground(imageBytes: ByteArray): Result<ProcessedImage> =
        withContext(Dispatchers.Default) {
            runCatching {
                val startTime = System.currentTimeMillis()

                Logger.debug("Decoding image with MLKit...")

                // Decode original image
                val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: throw ImageDecodingException("Failed to decode image bytes")

                val originalWidth = originalBitmap.width
                val originalHeight = originalBitmap.height

                Logger.debug("Original image: ${originalWidth}x${originalHeight}")

                // Create MLKit InputImage
                val inputImage = InputImage.fromBitmap(originalBitmap, 0)

                // Run segmentation
                Logger.debug("Running MLKit subject segmentation...")
                val segmentationResult = segmenter.process(inputImage).await()

                // Get foreground confidence mask (FloatBuffer)
                val foregroundMask = segmentationResult.foregroundConfidenceMask
                    ?: throw InferenceException("No foreground mask returned from MLKit")

                // Extract foreground with transparency
                Logger.debug("Extracting foreground...")
                val resultBitmap = createTransparentForeground(
                    originalBitmap,
                    foregroundMask,
                    segmentationResult.foregroundBitmap?.width ?: originalWidth,
                    segmentationResult.foregroundBitmap?.height ?: originalHeight
                )

                // Encode to PNG
                Logger.debug("Encoding result...")
                val outputStream = ByteArrayOutputStream()
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                val outputBytes = outputStream.toByteArray()

                val processingTime = System.currentTimeMillis() - startTime

                val inputMB = imageBytes.size / 1024.0 / 1024.0
                val outputMB = outputBytes.size / 1024.0 / 1024.0
                Logger.debug("✅ MLKit processing completed in ${processingTime}ms")
                Logger.debug("📦 Size: %.2fMB → %.2fMB".format(inputMB, outputMB))

                ProcessedImage(
                    imageBytes = outputBytes,
                    width = originalWidth,
                    height = originalHeight,
                    processingTimeMs = processingTime
                )
            }.onFailure { e ->
                Logger.error("MLKit background removal failed", e)
            }
        }

    /**
     * Create transparent foreground bitmap from MLKit FloatBuffer mask
     */
    private fun createTransparentForeground(
        original: Bitmap,
        maskBuffer: FloatBuffer,
        maskWidth: Int,
        maskHeight: Int
    ): Bitmap {
        Logger.debug("Creating transparent foreground: ${maskWidth}x${maskHeight}")

        // Create result bitmap with alpha channel
        val result = createBitmap(maskWidth, maskHeight)

        // Rewind buffer to start
        maskBuffer.rewind()

        // Scale original if sizes don't match
        val scaledOriginal = if (original.width != maskWidth || original.height != maskHeight) {
            Logger.debug("Scaling original from ${original.width}x${original.height} to ${maskWidth}x${maskHeight}")
            original.scale(maskWidth, maskHeight)
        } else {
            original
        }

        // Get all pixels at once (faster)
        val pixels = IntArray(maskWidth * maskHeight)
        scaledOriginal.getPixels(pixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)

        val resultPixels = IntArray(maskWidth * maskHeight)

        // Apply mask
        for (i in pixels.indices) {
            // Get mask confidence (0.0 = background, 1.0 = foreground)
            val confidence = maskBuffer.get()

            // Convert confidence to alpha (0-255)
            val alpha = (confidence * 255).toInt().coerceIn(0, 255)

            // Get original pixel color
            val originalPixel = pixels[i]

            // Create new pixel with alpha from mask
            resultPixels[i] = Color.argb(
                alpha,
                Color.red(originalPixel),
                Color.green(originalPixel),
                Color.blue(originalPixel)
            )
        }

        // Set all pixels at once
        result.setPixels(resultPixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)

        // Clean up scaled bitmap if created
        if (scaledOriginal != original) {
            scaledOriginal.recycle()
        }

        Logger.debug("✅ Transparent foreground created")

        return result
    }





    override fun close() {
        try {
            segmenter.close()
            Logger.debug("MLKit segmenter closed")
        } catch (e: Exception) {
            Logger.debug("Segmenter already closed")
        }
    }
}