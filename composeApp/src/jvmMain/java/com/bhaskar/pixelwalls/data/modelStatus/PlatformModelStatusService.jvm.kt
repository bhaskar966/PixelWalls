package com.bhaskar.pixelwalls.data.modelStatus

import com.bhaskar.pixelwalls.domain.service.ModelStatus
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class PlatformModelStatusService : ModelStatusService {
    actual override val status: StateFlow<ModelStatus> = MutableStateFlow(ModelStatus.Ready)

    actual override fun checkStatus() {}

    actual override fun downloadModels() {}
}