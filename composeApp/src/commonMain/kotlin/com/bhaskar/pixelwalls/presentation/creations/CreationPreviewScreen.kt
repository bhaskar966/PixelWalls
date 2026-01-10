package com.bhaskar.pixelwalls.presentation.creations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.bhaskar.pixelwalls.presentation.components.FullScreenTopBar
import com.bhaskar.pixelwalls.presentation.components.WallpaperActionsDialog

@Composable
fun CreationPreviewScreen(
    imagePath: String,
    navController: NavHostController,
    state: CreationsUiState,
    onEvent: (CreationsUiEvents) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = imagePath,
            contentDescription = "Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        FullScreenTopBar(
            onBackClick = { navController.popBackStack() },
            onActionClick = {
                onEvent(CreationsUiEvents.OnPreviewActionClick(imagePath))
            }
        )

        if (state.showWallpaperDialog) {
            WallpaperActionsDialog(
                showSaveToGallery = false,
                isOperating = state.isOperating,
                currentStep = state.currentActionStep,
                wallpaperResult = state.wallpaperResult,
                isShareSupported = state.isShareSupported,
                onDismiss = { onEvent(CreationsUiEvents.OnDismissDialog) },
                onSetWallpaper = { onEvent(CreationsUiEvents.OnSetWallpaper(it)) },
                onShare = { onEvent(CreationsUiEvents.OnShareClick) },
                onLocate = { onEvent(CreationsUiEvents.OnLocateClick) }
            )
        }
    }
}
