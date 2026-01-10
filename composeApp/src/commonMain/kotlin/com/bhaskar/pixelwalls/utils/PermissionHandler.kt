package com.bhaskar.pixelwalls.utils

import androidx.compose.runtime.Composable

expect class PermissionHandler {
    suspend fun checkStoragePermission(): Boolean
    suspend fun requestStoragePermission(): Boolean
    suspend fun requestDeletePermission(uris: List<String>): Boolean
}

@Composable
expect fun rememberPermissionHandler(): PermissionHandler