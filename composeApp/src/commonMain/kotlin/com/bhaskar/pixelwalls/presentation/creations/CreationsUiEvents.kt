package com.bhaskar.pixelwalls.presentation.creations

import com.bhaskar.pixelwalls.domain.service.WallpaperTarget

sealed class CreationsUiEvents {
    data object LoadCreations : CreationsUiEvents()
    data object RefreshCreations : CreationsUiEvents()
    data class DeleteCreation(val path: String) : CreationsUiEvents()
    data object RequestPermission: CreationsUiEvents()
    data object DismissError: CreationsUiEvents()

    data class OnPreviewActionClick(val path: String) : CreationsUiEvents()
    data class OnSetWallpaper(val target: WallpaperTarget? = null) : CreationsUiEvents()
    data object OnShareClick : CreationsUiEvents()
    data object OnLocateClick : CreationsUiEvents()
    data object OnDismissDialog : CreationsUiEvents()
}
