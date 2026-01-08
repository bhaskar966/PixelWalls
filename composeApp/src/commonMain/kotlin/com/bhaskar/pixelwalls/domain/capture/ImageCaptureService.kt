package com.bhaskar.pixelwalls.domain.capture

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.Flow

interface ImageCaptureService {

    suspend fun captureGraphicsLayer(
        graphicsLayer: GraphicsLayer
    ): Result<ByteArray>


    @Composable
    fun rememberCapturableContent(
        content: @Composable () -> Unit
    ): CaptureableContent

}