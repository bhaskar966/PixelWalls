package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.domain.WallpaperSetResult
import com.bhaskar.pixelwalls.domain.WallpaperSetter
import com.bhaskar.pixelwalls.domain.WallpaperTarget
import com.bhaskar.pixelwalls.domain.capture.ImageSaveService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

enum class ActionStep { Main, TargetSelection, Result }

// D:/leaning/Kotlin/PixelWalls/composeApp/src/commonMain/kotlin/com/bhaskar/pixelwalls/presentation/editor/controlPanel/WallpaperActions.kt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun WallpaperActions(
    imageBytes: ByteArray,
    onDismiss: () -> Unit
) {
    val wallpaperSetter = koinInject<WallpaperSetter>()
    val saveService = koinInject<ImageSaveService>()
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(ActionStep.Main) }
    var resultMessage by remember { mutableStateOf<WallpaperSetResult?>(null) }
    var isOperating by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                when (currentStep) {
                    ActionStep.Main -> {
                        Text("Wallpaper Actions", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (wallpaperSetter.canSetWallpaperDirectly()) {
                                    if (wallpaperSetter.canApplyWallpaperInDifferentScreens) {
                                        currentStep = ActionStep.TargetSelection
                                    } else {
                                        scope.launch {
                                            isOperating = true
                                            resultMessage = wallpaperSetter.setWallpaper(imageBytes, WallpaperTarget.HOME_SCREEN)
                                            currentStep = ActionStep.Result
                                            isOperating = false
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        isOperating = true
                                        resultMessage = wallpaperSetter.setWallpaper(imageBytes)
                                        currentStep = ActionStep.Result
                                        isOperating = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Set as Wallpaper") }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isOperating = true
                                    saveService.saveToGallery("PixelWall_${Clock.System.now().toEpochMilliseconds()}", imageBytes)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Save to Gallery") }

                        if (saveService.isShareSupported) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isOperating = true
                                        saveService.shareImage("PixelWall_Share", imageBytes)
                                        isOperating = false
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Share") }
                        }
                    }

                    ActionStep.TargetSelection -> {
                        Text("Select Target", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))

                        val targets = listOf(
                            "Home Screen" to WallpaperTarget.HOME_SCREEN,
                            "Lock Screen" to WallpaperTarget.LOCK_SCREEN,
                            "Both" to WallpaperTarget.BOTH
                        )

                        targets.forEach { (label, target) ->
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        isOperating = true
                                        resultMessage = wallpaperSetter.setWallpaper(imageBytes, target)
                                        currentStep = ActionStep.Result
                                        isOperating = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(label) }
                        }
                    }

                    ActionStep.Result -> {
                        // ... Result UI stays the same ...
                        val res = resultMessage
                        Text(
                            text = when(res) {
                                is WallpaperSetResult.Success -> "Done!"
                                is WallpaperSetResult.UserActionRequired -> "One more step"
                                else -> "Error"
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = when(res) {
                                is WallpaperSetResult.Success -> "Wallpaper applied successfully."
                                is WallpaperSetResult.UserActionRequired -> res.instructions
                                is WallpaperSetResult.Error -> res.message
                                else -> ""
                            }
                        )
                        Spacer(Modifier.height(16.dp))

                        if (res is WallpaperSetResult.UserActionRequired && res.instructions.contains("saved to")) {
                            Button(onClick = {
                                scope.launch { wallpaperSetter.openWallpaperPicker(imageBytes) }
                            }) { Text("Locate Wallpaper") }
                        }

                        Button(onClick = onDismiss, modifier = Modifier.align(androidx.compose.ui.Alignment.End)) {
                            Text("Close")
                        }
                    }
                }

                if (isOperating) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }
        }
    }
}

