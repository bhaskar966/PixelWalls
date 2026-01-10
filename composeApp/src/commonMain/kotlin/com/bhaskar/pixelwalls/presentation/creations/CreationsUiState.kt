package com.bhaskar.pixelwalls.presentation.creations

data class CreationsUiState(
    val wallpapers: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val needsPermission: Boolean = true
)