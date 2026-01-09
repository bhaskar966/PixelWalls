package com.bhaskar.pixelwalls.data.save

import com.bhaskar.pixelwalls.domain.service.ImageFormat
import com.bhaskar.pixelwalls.domain.service.ImageSaveService
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume

actual class PlatformImageSaveService : ImageSaveService {

    actual override val isShareSupported: Boolean = true

    actual override suspend fun saveToGallery(
        fileName: String,
        imageBytes: ByteArray,
        format: ImageFormat
    ): Result<String> = suspendCancellableCoroutine { cont ->
        val image = createUIImage(imageBytes)
            ?: return@suspendCancellableCoroutine cont.resume(
                Result.failure(Exception("Invalid image data"))
            )

        PHPhotoLibrary.requestAuthorization { status ->
            if(status != PHAuthorizationStatusAuthorized) {
                cont.resume(Result.failure(Exception("Permission not granted")))
                return@requestAuthorization
            }

            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                val request = PHAssetChangeRequest.creationRequestForAssetFromImage(image)
                addToPixelWallsAlbum(request?.placeholderForCreatedAsset?.localIdentifier)
            }) { success, error ->
                if(success) cont.resume(Result.success("photos://saved"))
                else cont.resume(Result.failure(Exception("Unknown error")))

            }

        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual override suspend fun saveToCache(
        fileName: String,
        imageBytes: ByteArray
    ): Result<String> {
        val cacheDir = NSFileManager.defaultManager.URLsForDirectory(
            NSCachesDirectory, NSUserDomainMask
        ).firstOrNull() as? String ?: return Result.failure(Exception("No cache"))

        val filePath = "$cacheDir/$fileName"
        imageBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = imageBytes.size.toULong())
                .writeToFile(filePath, true)
        }
        return Result.success(filePath)
    }

    actual override suspend fun shareImage(
        fileName: String,
        imageBytes: ByteArray
    ): Result<Unit> {
        val image = createUIImage(imageBytes) ?: return Result.failure(Exception("Invalid image"))
        // UIActivityViewController implementation
        return Result.success(Unit)
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

    private fun addToPixelWallsAlbum(assetId: String?) {
        TODO()
    }

}