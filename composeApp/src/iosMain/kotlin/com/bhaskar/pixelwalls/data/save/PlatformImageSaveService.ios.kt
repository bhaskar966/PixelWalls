package com.bhaskar.pixelwalls.data.save

import androidx.compose.ui.graphics.vector.path
import com.bhaskar.pixelwalls.domain.service.ImageFormat
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.utils.PixelWallsPaths
import com.bhaskar.pixelwalls.utils.getPublicPicturesDir
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.popoverPresentationController
import kotlin.coroutines.resume

actual class PlatformImageSaveService : ImageSaveService {

    actual override val isShareSupported: Boolean = true

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> = suspendCancellableCoroutine { cont ->
        val image = createUIImage(imageBytes)
            ?: return@suspendCancellableCoroutine cont.resume(
                Result.failure(Exception("Invalid image data"))
            )

        val internalSavedPath = try {
            val baseDir = getPublicPicturesDir()
            val pixelWallsPaths = "$baseDir/${PixelWallsPaths.FOLDER_NAME}"

            NSFileManager.defaultManager.createDirectoryAtPath(pixelWallsPaths, true, null, null)

            val filePath = "$pixelWallsPaths/${fileName}.${format.extension}"
            val data = imageBytes.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = imageBytes.size.toULong()
                )
            }

            data.writeToFile(filePath, atomically = true)
            filePath
        } catch (e: Exception) {
            null
        }

        PHPhotoLibrary.requestAuthorization { status ->
            if(status != PHAuthorizationStatusAuthorized) {
                cont.resume(Result.success(internalSavedPath ?: "internal://saved"))
                return@requestAuthorization
            }

            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                PHAssetChangeRequest.creationRequestForAssetFromImage(image)
            }) { success, error ->
                if (success) {
                    cont.resume(Result.success(internalSavedPath ?: "photos://saved"))
                } else {
                    cont.resume(Result.failure(Exception(error?.localizedDescription ?: "Gallery save failed")))
                }
            }

        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> {
        return try {
            val cacheDir = NSSearchPathForDirectoriesInDomains(
                directory = NSCachesDirectory,
                domainMask = NSUserDomainMask,
                expandTilde = true
            ).firstOrNull() as? String ?: throw IllegalStateException("Could not find cache directory")

            val filePath = "$cacheDir/$fileName"

            val data = imageBytes.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = imageBytes.size.toULong()
                )
            }

            val saved = data.writeToFile(filePath, true)
            if (saved) {
                Result.success(filePath)
            } else {
                Result.failure(Exception("Failed to write data to cache"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit> {
        val image = createUIImage(imageBytes) ?: return Result.failure(Exception("Invalid image"))

        val window = UIApplication.sharedApplication.keyWindow
        val rootViewerController = window?.rootViewController

        if(rootViewerController != null) {

            val activityController = UIActivityViewController(
                activityItems = listOf(image),
                applicationActivities = null
            )

            activityController.popoverPresentationController?.sourceView = rootViewerController.view

            rootViewerController.presentViewController(
                viewControllerToPresent = activityController,
                animated = true,
                completion = null
            )

            return Result.success(Unit)
        }

        return Result.failure(Exception("Root view controller not found"))
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun createUIImage(bytes: ByteArray): UIImage? {
        val data = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
        }
        return UIImage.imageWithData(data)
    }

}