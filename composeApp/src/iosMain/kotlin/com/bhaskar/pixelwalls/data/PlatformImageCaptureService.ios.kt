package com.bhaskar.pixelwalls.data

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import com.bhaskar.pixelwalls.domain.capture.ImageFormat
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual fun ImageBitmap.toByteArray(format: ImageFormat): ByteArray {
    val sikaBitmap = this.asSkiaBitmap()
    val image = Image.makeFromBitmap(sikaBitmap)
    val sikaFormat = when(format) {
        ImageFormat.PNG -> EncodedImageFormat.PNG
        ImageFormat.JPEG -> EncodedImageFormat.JPEG
    }
    return image.encodeToData(sikaFormat, 100)?.bytes
        ?: throw Exception("Failed to encode Skia Image")

}