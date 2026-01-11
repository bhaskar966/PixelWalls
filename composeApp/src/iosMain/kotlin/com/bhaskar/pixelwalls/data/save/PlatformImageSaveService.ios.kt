package com.bhaskar.pixelwalls.data.save

import com.bhaskar.pixelwalls.domain.service.ImageFormat
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import com.bhaskar.pixelwalls.utils.PixelWallsPaths
import com.bhaskar.pixelwalls.utils.getPublicPicturesDir
import io.github.vinceglb.filekit.utils.toByteArray
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSThread
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class PlatformImageSaveService : ImageSaveService {

    actual override val isShareSupported: Boolean = true

    actual override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> = performIosSave(
        bytes = imageBytes,
        name = fileName,
        format = format
    )

    actual override suspend fun saveToGallery(
        fileName: String,
        filePath: String,
        format: ImageFormat
    ): Result<String> = performIosSave(
        bytes = NSData
            .dataWithContentsOfFile(filePath)
            ?.toByteArray()
            ?: byteArrayOf(),
        name = fileName,
        format = format
    )

    private suspend fun performIosSave(
        bytes: ByteArray,
        name: String,
        format: ImageFormat
    ): Result<String> = suspendCancellableCoroutine { cont ->

        val image = createUIImage(bytes)
            ?: return@suspendCancellableCoroutine cont.resume(
                Result.failure(Exception("Invalid image data"))
            )

        val internalSavedPath = try {
            val baseDir = getPublicPicturesDir()
            val pixelWallsPaths = "$baseDir/${PixelWallsPaths.FOLDER_NAME}"

            NSFileManager.defaultManager.createDirectoryAtPath(pixelWallsPaths, true, null, null)

            val filePath = "$pixelWallsPaths/${name}.${format.extension}"
            val data = bytes.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = bytes.size.toULong()
                )
            }

            data.writeToFile(filePath, atomically = true)
            filePath
        } catch (e: Exception) {
            e.printStackTrace()
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

    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> = performSaveToCache(
        fileName = fileName,
        bytes = imageBytes
    )

    actual override suspend fun saveToCache(
        fileName: String,
        filePath: String
    ): Result<String> = performSaveToCache(
        fileName = fileName,
        bytes = NSData
            .dataWithContentsOfFile(filePath)
            ?.toByteArray()
            ?: byteArrayOf()
    )

    private fun performSaveToCache(
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        return try {
            val cacheDir = NSSearchPathForDirectoriesInDomains(
                directory = NSCachesDirectory,
                domainMask = NSUserDomainMask,
                expandTilde = true
            ).firstOrNull() as? String ?: throw IllegalStateException("Could not find cache directory")

            val filePath = "$cacheDir/$fileName"

            val data = bytes.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = bytes.size.toULong()
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
    ): Result<Unit> = withContext(Dispatchers.Main) {
        val image = createUIImage(imageBytes)
            ?: return@withContext Result.failure(Exception("Failed to decode image for sharing"))

        return@withContext performShare(image)
    }


    actual override suspend fun shareImage(
        fileName: String,
        filePath: String
    ): Result<Unit> = performShare(image = UIImage.imageWithContentsOfFile(filePath))

    private fun performShare(image: UIImage?): Result<Unit> {
        if (image == null) return Result.failure(Exception("Could not load image"))

        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController

        if (rootViewController != null) {
            val activityController = UIActivityViewController(
                activityItems = listOf(image),
                applicationActivities = null
            )

            activityController.popoverPresentationController?.sourceView = rootViewController.view

            rootViewController.presentViewController(
                viewControllerToPresent = activityController,
                animated = true,
                completion = null
            )
            return Result.success(Unit)
        }

        return Result.failure(Exception("Unable to find iOS RootViewController"))
    }

    private fun createUIImage(bytes: ByteArray): UIImage? {
        if (bytes.isEmpty()) return null

        return bytes.usePinned { pinned ->
            val data = NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
            UIImage.imageWithData(data)
        }
    }

}