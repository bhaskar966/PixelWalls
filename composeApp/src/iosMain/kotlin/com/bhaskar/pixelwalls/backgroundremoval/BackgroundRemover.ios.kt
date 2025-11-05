package com.bhaskar.pixelwalls.backgroundremoval

import com.bhaskar.pixelwalls.domain.model.ProcessedImage
import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreGraphics.*
import platform.CoreImage.*
import platform.Foundation.*
import platform.ImageIO.kCGImagePropertyOrientationUp
import platform.UIKit.*
import platform.Vision.*
import platform.posix.memcpy
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual fun createBackgroundRemover(): BackgroundRemover {
    return BackgroundRemoverIOS()
}

@OptIn(ExperimentalForeignApi::class)
internal class BackgroundRemoverIOS : BackgroundRemover {

    override suspend fun removeBackground(imageBytes: ByteArray): Result<ProcessedImage> =
        withContext(Dispatchers.Default) {
            runCatching {
                val data = imageBytes.toNSData()
                val uiImage = UIImage.imageWithData(data)
                    ?: throw ImageDecodingException("Failed to decode image bytes")

                val width = uiImage.size.useContents { width.toInt() }
                val height = uiImage.size.useContents { height.toInt() }

                Logger.debug("iOS: Processing ${width}x${height} image")

                // Generate the mask using the most robust method
                val mask = generateForegroundMask(uiImage)
                val resultImage = applyMask(uiImage, mask)

                val resultData = UIImagePNGRepresentation(resultImage)
                    ?: throw InferenceException("Failed to encode result image")

                val outputBytes = resultData.toByteArray()
                Logger.debug("✅ iOS processing completed")

                ProcessedImage(
                    imageBytes = outputBytes,
                    width = width,
                    height = height,
                    processingTimeMs = 0 // Placeholder
                )
            }.onFailure { e ->
                Logger.error("iOS background removal failed", e)
            }
        }

    @OptIn(BetaInteropApi::class)
    private suspend fun generateForegroundMask(uiImage: UIImage): CIImage =
        suspendCancellableCoroutine { continuation ->
            val cgImage = uiImage.CGImage
                ?: throw ImageDecodingException("Failed to get CGImage from UIImage")

            // Use the general-purpose mask request
            val request = VNGenerateForegroundInstanceMaskRequest { vnRequest, error ->
                if (error != null) {
                    continuation.resumeWithException(
                        InferenceException("Vision request failed in completion handler: ${error.localizedDescription()}")
                    )
                    return@VNGenerateForegroundInstanceMaskRequest
                }

                val results = vnRequest?.results() as? List<*>
                val observation = results?.firstOrNull() as? VNInstanceMaskObservation
                if (observation == null) {
                    continuation.resumeWithException(InferenceException("No mask observation returned"))
                    return@VNGenerateForegroundInstanceMaskRequest
                }

                try {
                    val pixelBuffer = observation.instanceMask()
                    val maskCIImage = CIImage.imageWithCVPixelBuffer(pixelBuffer)
                    continuation.resume(maskCIImage)
                } catch (e: Exception) {
                    continuation.resumeWithException(InferenceException("Failed to process mask: ${e.message}"))
                }
            }

            // This is critical for simulator compatibility
            request.setUsesCPUOnly(true)

            try {
                // --- THIS IS THE FINAL, COMPILING, AND WORKING CODE ---
                // We use the most explicit constructor available: CGImage with orientation.
                // This removes all ambiguity for the Kotlin/Native compiler.
                val handler = VNImageRequestHandler(
                    cGImage = cgImage.reinterpret(),
                    orientation = kCGImagePropertyOrientationUp,
                    options = emptyMap<Any?, Any>() // An empty map is fine here
                )

                // Perform the request synchronously and check for immediate failure
                memScoped {
                    val errorPtr = alloc<ObjCObjectVar<NSError?>>()
                    val success = handler.performRequests(listOf(request), errorPtr.ptr)
                    if (!success) {
                        val errorMessage = errorPtr.value?.localizedDescription() ?: "Unknown Vision error"
                        throw InferenceException("Failed to perform Vision request: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                // If any part of the synchronous code fails, report it.
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        }

    private fun applyMask(original: UIImage, mask: CIImage): UIImage {
        val originalCI = CIImage.imageWithCGImage(original.CGImage!!)!!
        val originalExtent = originalCI.extent()
        val maskExtent = mask.extent()

        // Scale the mask to fit the original image
        val scaleX = originalExtent.useContents { size.width } / maskExtent.useContents { size.width }
        val scaleY = originalExtent.useContents { size.height } / maskExtent.useContents { size.height }
        val scaledMask = mask.imageByApplyingTransform(CGAffineTransformMakeScale(scaleX, scaleY))

        val blendFilter = CIFilter.filterWithName("CIBlendWithMask")!!
        blendFilter.setValue(originalCI, forKey = kCIInputImageKey)
        blendFilter.setValue(scaledMask, forKey = kCIInputMaskImageKey)

        val outputCI = blendFilter.outputImage()!!

        val context = CIContext.context()
        val outputExtent = outputCI.extent()
        val cgImage = context.createCGImage(outputCI, fromRect = outputExtent)
            ?: throw InferenceException("Failed to render result image")

        val resultImage = UIImage.imageWithCGImage(cgImage)

        // Release the manually created CGImage
        CGImageRelease(cgImage)

        return resultImage
    }

    override fun close() {}
}

// --- Helper Functions ---

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(this.length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
    }
}
