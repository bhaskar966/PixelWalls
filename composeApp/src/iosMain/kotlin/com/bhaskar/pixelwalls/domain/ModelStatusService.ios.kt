package com.bhaskar.pixelwalls.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class PlatformModelStatusService : ModelStatusService {
    actual override val status: StateFlow<ModelStatus> = MutableStateFlow(ModelStatus.Unsupported)

    actual override fun checkStatus() {}

    actual override fun downloadModels() {}
}