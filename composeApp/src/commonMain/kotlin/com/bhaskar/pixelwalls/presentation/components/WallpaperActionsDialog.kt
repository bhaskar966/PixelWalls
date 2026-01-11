package com.bhaskar.pixelwalls.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.ActionStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperActionsDialog(
    showSaveToGallery: Boolean,
    isOperating: Boolean,
    currentStep: ActionStep,
    wallpaperResult: WallpaperSetResult?,
    isShareSupported: Boolean,
    onDismiss: () -> Unit,
    onSetWallpaper: (WallpaperTarget?) -> Unit,
    onShare: () -> Unit,
    onSaveToGallery: () -> Unit = {},
    onLocate: () -> Unit = {}
) {
    BasicAlertDialog(
        onDismissRequest = { if (!isOperating) onDismiss() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(min = 280.dp, max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (currentStep) {
                    ActionStep.Main -> {
                        Text(
                            "Actions",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { onSetWallpaper(null) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = !isOperating
                        ) { Text("Set as Wallpaper") }

                        if (showSaveToGallery) {
                            OutlinedButton(
                                onClick = onSaveToGallery,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                enabled = !isOperating
                            ) { Text("Save to Gallery") }
                        }

                        if (isShareSupported) {
                            OutlinedButton(
                                onClick = onShare,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                enabled = !isOperating
                            ) { Text("Share") }
                        }
                    }

                    ActionStep.TargetSelection -> {
                        Text(
                            "Select Target",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                "Home Screen" to WallpaperTarget.HOME_SCREEN,
                                "Lock Screen" to WallpaperTarget.LOCK_SCREEN,
                                "Both" to WallpaperTarget.BOTH
                            ).forEach { (label, target) ->
                                TextButton(
                                    onClick = { onSetWallpaper(target) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    enabled = !isOperating
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }

                    ActionStep.Result -> {
                        val title = when (wallpaperResult) {
                            is WallpaperSetResult.Success -> "Success!"
                            is WallpaperSetResult.UserActionRequired -> "Action Needed"
                            is WallpaperSetResult.Error -> "Error"
                            null -> "Processing..."
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        val description = when (wallpaperResult) {
                            is WallpaperSetResult.Success -> "Operation completed successfully."
                            is WallpaperSetResult.UserActionRequired -> wallpaperResult.instructions
                            is WallpaperSetResult.Error -> wallpaperResult.message
                            null -> "Please wait..."
                        }

                        Text(
                            text = description,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

//                        if (wallpaperResult is WallpaperSetResult.UserActionRequired &&
//                            wallpaperResult.instructions.contains("saved to")) {
//                            Button(
//                                onClick = onLocate,
//                                modifier = Modifier.fillMaxWidth().height(48.dp)
//                            ) {
//                                Text("Locate Wallpaper")
//                            }
//                        }

                        Button(
                            onClick = { onDismiss() },
                            modifier = Modifier.align(Alignment.End).height(48.dp),
                            enabled = !isOperating
                        ) { Text("Close") }
                    }
                }

                if (isOperating) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
