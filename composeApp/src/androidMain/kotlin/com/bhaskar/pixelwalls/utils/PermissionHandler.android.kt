package com.bhaskar.pixelwalls.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import androidx.core.net.toUri

actual class PermissionHandler(
    private val context: Context,
) {

    internal var permissionLauncher: ActivityResultLauncher<String>? = null
    internal var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    private var permissionResultDeferred: CompletableDeferred<Boolean>? = null
    private var deleteResultDeferred: CompletableDeferred<Boolean>? = null

    fun onPermissionResult(isGranted: Boolean) {
        permissionResultDeferred?.complete(isGranted)
        permissionResultDeferred = null
    }

    fun onDeleteResult(success: Boolean) {
        deleteResultDeferred?.complete(success)
        deleteResultDeferred = null
    }

    actual suspend fun checkStoragePermission(): Boolean {
        val permission = getStoragePermission()
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        return granted
    }

    actual suspend fun requestStoragePermission(): Boolean {
        val permission = getStoragePermission()

        val alreadyGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) return true

        permissionResultDeferred = CompletableDeferred()
        permissionLauncher?.launch(permission)

        val result = permissionResultDeferred!!.await()
        return result
    }

    private fun getStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    actual suspend fun requestDeletePermission(uris: List<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val contentUris = uris.map { it.toUri() }

                val intentSender = MediaStore.createDeleteRequest(
                    context.contentResolver,
                    contentUris
                ).intentSender

                deleteResultDeferred = CompletableDeferred()

                deleteLauncher?.launch(
                    IntentSenderRequest.Builder(intentSender).build()
                )

                val result = deleteResultDeferred!!.await()
                return result
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        } else {
            return true
        }
    }
}

@Composable
actual fun rememberPermissionHandler(): PermissionHandler {
    val context = LocalContext.current

    val handler = remember(context) {
        PermissionHandler(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handler.onPermissionResult(isGranted)
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val success = result.resultCode == android.app.Activity.RESULT_OK
        handler.onDeleteResult(success)
    }

    handler.permissionLauncher = permissionLauncher
    handler.deleteLauncher = deleteLauncher

    return handler
}