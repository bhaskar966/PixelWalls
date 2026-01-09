package com.bhaskar.pixelwalls.data.modelStatus

import com.bhaskar.pixelwalls.domain.service.ModelStatus
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import kotlinx.coroutines.flow.StateFlow

expect class PlatformModelStatusService: ModelStatusService {
    override val status: StateFlow<ModelStatus>
    override fun checkStatus()
    override fun downloadModels()
}