package com.bhaskar.pixelwalls.presentation.creations

sealed class CreationsUiEvents {
    data object LoadCreations : CreationsUiEvents()
    data object RefreshCreations : CreationsUiEvents()
    data class DeleteCreation(val path: String) : CreationsUiEvents()
    data object RequestPermission: CreationsUiEvents()
    data object DismissError: CreationsUiEvents()
}
