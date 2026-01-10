package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.domain.model.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.service.WallpaperTarget
import com.bhaskar.pixelwalls.presentation.editor.EditorState
import com.bhaskar.pixelwalls.presentation.editor.EditorUiEvents
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.ActionStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperActions(
    state: EditorState,
    onEvent: (EditorUiEvents) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {
            if (!state.isOperating) {
                onEvent(EditorUiEvents.OnDismissDialog)
            }
        }
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state.currentActionStep) {
                    ActionStep.Main -> {
                        Text(
                            text = "Wallpaper Actions",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { onEvent(EditorUiEvents.OnSetWallpaperClick()) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isOperating
                        ) {
                            Text("Set as Wallpaper")
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onEvent(EditorUiEvents.OnSaveToGalleryClick) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isOperating
                        ) {
                            Text("Save to Gallery")
                        }

                        if (state.isShareSupported) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { onEvent(EditorUiEvents.OnShareClick) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isOperating
                            ) {
                                Text("Share")
                            }
                        }
                    }

                    ActionStep.TargetSelection -> {
                        Text(
                            text = "Select Target",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(16.dp))

                        val targets = listOf(
                            "Home Screen" to WallpaperTarget.HOME_SCREEN,
                            "Lock Screen" to WallpaperTarget.LOCK_SCREEN,
                            "Both" to WallpaperTarget.BOTH
                        )

                        targets.forEach { (label, target) ->
                            TextButton(
                                onClick = { onEvent(EditorUiEvents.OnSetWallpaperClick(target)) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isOperating
                            ) {
                                Text(label)
                            }
                        }
                    }

                    ActionStep.Result -> {
                        val res = state.wallpaperResult

                        Text(
                            text = when (res) {
                                is WallpaperSetResult.Success -> "Done!"
                                is WallpaperSetResult.UserActionRequired -> "Action Needed"
                                is WallpaperSetResult.Error -> "Error"
                                null -> "Processing..."
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = when (res) {
                                is WallpaperSetResult.Success -> {
                                    "Operation completed successfully."
                                }
                                is WallpaperSetResult.UserActionRequired -> res.instructions
                                is WallpaperSetResult.Error -> res.message
                                null -> "Please wait a moment..."
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(24.dp))

                        if (res is WallpaperSetResult.UserActionRequired && res.instructions.contains("saved to")) {
                            Button(
                                onClick = { onEvent(EditorUiEvents.OnLocateWallpaperClick) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Locate Wallpaper")
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { onEvent(EditorUiEvents.OnDismissDialog) },
                            modifier = Modifier.align(Alignment.End),
                            enabled = !state.isOperating
                        ) {
                            Text("Close")
                        }
                    }
                }

                if (state.isOperating) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
