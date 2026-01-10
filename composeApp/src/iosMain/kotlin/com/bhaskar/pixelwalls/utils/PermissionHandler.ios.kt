package com.bhaskar.pixelwalls.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHPhotoLibrary
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class PermissionHandler {
    actual suspend fun checkStoragePermission(): Boolean {
        val status = PHPhotoLibrary.authorizationStatus()
        return status == PHAuthorizationStatusAuthorized ||
                status == PHAuthorizationStatusLimited
    }

    actual suspend fun requestStoragePermission(): Boolean = suspendCoroutine { continuation ->
        val currentStatus = PHPhotoLibrary.authorizationStatus()

        if (currentStatus == PHAuthorizationStatusAuthorized ||
            currentStatus == PHAuthorizationStatusLimited) {
            continuation.resume(true)
            return@suspendCoroutine
        }

        if (currentStatus == PHAuthorizationStatusDenied ||
            currentStatus == PHAuthorizationStatusRestricted) {
            continuation.resume(false)
            return@suspendCoroutine
        }

        PHPhotoLibrary.requestAuthorization { status ->
            val granted = status == PHAuthorizationStatusAuthorized ||
                    status == PHAuthorizationStatusLimited
            continuation.resume(granted)
        }
    }

    actual suspend fun requestDeletePermission(uris: List<String>): Boolean = true
}

@Composable
actual fun rememberPermissionHandler(): PermissionHandler {
    return remember { PermissionHandler() }
}