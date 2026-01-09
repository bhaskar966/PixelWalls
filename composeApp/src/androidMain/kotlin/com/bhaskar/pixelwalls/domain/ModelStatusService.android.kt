package com.bhaskar.pixelwalls.domain

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class PlatformModelStatusService(
    private val context: Context
) : ModelStatusService {

    private val _status = MutableStateFlow<ModelStatus>(ModelStatus.NotDownloaded)
    actual override val status: StateFlow<ModelStatus> = _status.asStateFlow()


    private val moduleInstallClient = ModuleInstall.getClient(context)

    private val segmentationModuleApi = SubjectSegmentation.getClient(
        SubjectSegmenterOptions.Builder().build()
    )

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    actual override fun checkStatus() {

        val gmsAvailability = GoogleApiAvailability
            .getInstance()
            .isGooglePlayServicesAvailable(context)

        if(gmsAvailability != ConnectionResult.SUCCESS) {
            _status.value = ModelStatus.GmsMissing
            return
        }

        moduleInstallClient
            .areModulesAvailable(segmentationModuleApi)
            .addOnSuccessListener { response ->
                if(response.areModulesAvailable()){
                    _status.value = ModelStatus.Ready
                } else {
                    _status.value = ModelStatus.NotDownloaded
                }
            }
            .addOnFailureListener {
                _status.value = ModelStatus.Error(it.message ?: "Failed to check model status")
            }
    }

    actual override fun downloadModels() {
        if (_status.value == ModelStatus.GmsMissing) return

        if(!isNetworkAvailable()) {
            _status.value = ModelStatus.NetworkError
            return
        }

        _status.value = ModelStatus.Downloading

        val request = ModuleInstallRequest.newBuilder()
            .addApi(segmentationModuleApi)
            .build()

        moduleInstallClient
            .installModules(request)
            .addOnSuccessListener { response ->
                if(response.areModulesAlreadyInstalled()) {
                    _status.value = ModelStatus.Ready
                } else {
                    _status.value = ModelStatus.Ready
                }
            }
            .addOnFailureListener {
                _status.value = ModelStatus.Error(it.message ?: "Download failed")
            }
    }
}