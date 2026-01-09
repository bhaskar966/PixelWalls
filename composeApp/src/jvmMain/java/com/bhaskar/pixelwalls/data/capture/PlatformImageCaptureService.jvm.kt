package com.bhaskar.pixelwalls.data.capture

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import com.bhaskar.pixelwalls.domain.service.ImageFormat
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual fun ImageBitmap.toByteArray(format: ImageFormat): ByteArray {
    val bufferedImage = this.toAwtImage()
    return ByteArrayOutputStream().use { stream ->
        ImageIO.write(bufferedImage, format.extension, stream)
        stream.toByteArray()
    }
}