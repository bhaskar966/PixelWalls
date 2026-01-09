package com.bhaskar.pixelwalls.domain.service

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.bhaskar.pixelwalls.domain.capture.CaptureableContent

interface ImageCaptureService {

    suspend fun captureGraphicsLayer(
        graphicsLayer: GraphicsLayer
    ): Result<ByteArray>


    @Composable
    fun rememberCapturableContent(
        content: @Composable () -> Unit
    ): CaptureableContent

}