package com.bhaskar.pixelwalls.domain.capture

import androidx.compose.runtime.Composable

data class CaptureableContent(
    val composable: @Composable () -> Unit,
    val capture: suspend () -> Result<ByteArray>
)