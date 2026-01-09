package com.bhaskar.pixelwalls.domain.service

import kotlinx.coroutines.flow.StateFlow


sealed interface ModelStatus{
    data object Ready: ModelStatus
    data object NotDownloaded: ModelStatus
    data object Downloading: ModelStatus
    data object GmsMissing: ModelStatus
    data object NetworkError: ModelStatus
    data class Error(val message: String): ModelStatus
    data object Unsupported: ModelStatus
}

interface ModelStatusService {

    val status: StateFlow<ModelStatus>
    fun checkStatus()
    fun downloadModels()
}