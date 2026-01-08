package com.bhaskar.pixelwalls.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.bhaskar.pixelwalls.domain.capture.ImageFormat
import java.io.ByteArrayOutputStream

actual fun ImageBitmap.toByteArray(format: ImageFormat): ByteArray {
    val bitmap = this.asAndroidBitmap()
    return ByteArrayOutputStream().use { stream ->
        val compressFormat = when(format) {
            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        }
        bitmap.compress(compressFormat, 100, stream)
        stream.toByteArray()
    }
}