package com.bhaskar.pixelwalls.data.wallpaper

import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.WallpaperSetter
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume

actual class PlatformWallpaperSetter : WallpaperSetter {

    actual override val canApplyWallpaperInDifferentScreens: Boolean = true

    actual override suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget
    ): WallpaperSetResult {
        return saveToPhotosAndProvideInstructions(createUIImage(imageBytes))
    }

    actual override suspend fun setWallpaper(
        filePath: String,
        target: WallpaperTarget
    ): WallpaperSetResult {
        return saveToPhotosAndProvideInstructions(
            UIImage.imageWithContentsOfFile(path = filePath)
        )
    }

    actual override fun canSetWallpaperDirectly(): Boolean = false

    actual override suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult {
        return saveToPhotosAndProvideInstructions(createUIImage(bytes = imageBytes))
    }

    actual override suspend fun openWallpaperPicker(path: String): WallpaperSetResult {
        return saveToPhotosAndProvideInstructions(
            UIImage.imageWithContentsOfFile(path = path)
        )
    }

    private suspend fun saveToPhotosAndProvideInstructions(
        image: UIImage?
    ): WallpaperSetResult = suspendCancellableCoroutine { cont ->

        if (image == null) {
            cont.resume(WallpaperSetResult.Error("Failed to load image"))
            return@suspendCancellableCoroutine
        }

        PHPhotoLibrary.requestAuthorization { status ->
            if (status != PHAuthorizationStatusAuthorized) {
                cont.resume(
                    WallpaperSetResult.Error("Photo library access denied")
                )
                return@requestAuthorization
            }

            // Save to Photos
            PHPhotoLibrary.sharedPhotoLibrary().performChanges({
                PHAssetChangeRequest.creationRequestForAssetFromImage(image)
            }) { success, error ->
                if (success) {
                    val instructions = """
                        Image saved to Photos!
                        
                        To set as wallpaper:
                        1. Open Photos app
                        2. Find the saved image
                        3. Tap Share button
                        4. Select "Use as Wallpaper"
                        5. Adjust position and tap "Set"
                    """.trimIndent()

                    cont.resume(WallpaperSetResult.UserActionRequired(instructions))
                } else {
                    cont.resume(
                        WallpaperSetResult.Error(
                            error?.localizedDescription ?: "Failed to save"
                        )
                    )
                }
            }
        }
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