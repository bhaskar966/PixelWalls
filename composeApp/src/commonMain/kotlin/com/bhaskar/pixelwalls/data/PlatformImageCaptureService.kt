package com.bhaskar.pixelwalls.data

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import com.bhaskar.pixelwalls.domain.capture.CaptureableContent
import com.bhaskar.pixelwalls.domain.capture.ImageCaptureService
import com.bhaskar.pixelwalls.domain.capture.ImageFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class PlatformImageCaptureService: ImageCaptureService {
    override suspend fun captureGraphicsLayer(graphicsLayer: GraphicsLayer): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val imageBitmap = graphicsLayer.toImageBitmap()
            val bytes = imageBitmap.toByteArray(format = ImageFormat.PNG)
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @Composable
    override fun rememberCapturableContent(content: @Composable (() -> Unit)): CaptureableContent {

        val graphicsLayer = rememberGraphicsLayer()

        val composable: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
            ){
                content()
            }
        }

        val capture: suspend () -> Result<ByteArray> = {
            captureGraphicsLayer(graphicsLayer)
        }

        return CaptureableContent(composable, capture)

    }
}

expect fun ImageBitmap.toByteArray(format: ImageFormat = ImageFormat.PNG): ByteArray