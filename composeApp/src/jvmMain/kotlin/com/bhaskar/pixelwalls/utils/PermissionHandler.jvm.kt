package com.bhaskar.pixelwalls.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class PermissionHandler {
    actual suspend fun checkStoragePermission(): Boolean = true
    actual suspend fun requestStoragePermission(): Boolean = true
    actual suspend fun requestDeletePermission(uris: List<String>): Boolean = true
}

@Composable
actual fun rememberPermissionHandler(): PermissionHandler {
    return remember { PermissionHandler() }
}