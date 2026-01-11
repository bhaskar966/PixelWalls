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

    actual override val canApplyWallpaperInDifferentScreens: Boolean = false

    actual override suspend fun setWallpaper(
        imageBytes: ByteArray,
        target: WallpaperTarget
    ): WallpaperSetResult {
        return provideIosInstructions()
    }

    actual override suspend fun setWallpaper(
        filePath: String,
        target: WallpaperTarget
    ): WallpaperSetResult {
        return provideIosInstructions()
    }

    actual override fun canSetWallpaperDirectly(): Boolean = false

    actual override suspend fun openWallpaperPicker(imageBytes: ByteArray): WallpaperSetResult {
        return provideIosInstructions()
    }

    actual override suspend fun openWallpaperPicker(path: String): WallpaperSetResult {
        return provideIosInstructions()
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

    private fun provideIosInstructions(): WallpaperSetResult {
        val instructions = """
            To set this as your wallpaper:
            
            1. Open the Photos app
            2. Find this image in your library
            3. Tap the Share button (bottom left)
            4. Scroll down and select "Use as Wallpaper"
            5. Adjust the position and tap "Set"
        """.trimIndent()

        return WallpaperSetResult.UserActionRequired(instructions)
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