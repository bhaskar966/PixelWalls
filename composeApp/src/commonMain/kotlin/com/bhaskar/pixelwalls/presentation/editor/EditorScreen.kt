package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.domain.service.ModelStatus
import com.bhaskar.pixelwalls.domain.service.ModelStatusService
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun EditorScreen(
    onImagePicked: (String) -> Unit,
    state: EditorState,
    onEvent: (EditorUiEvents) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            "Live Wallpaper Editor",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            "Create stunning live wallpapers with shape effects.\n" +
                    "Your subject emerges from Material You shapes!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Feature cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "✨ Shape Effects",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Choose from Material 3 shapes:\n• Rounded Square\n• Circle\n• Squircle\n• Pill\n• Asymmetric",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🎨 Background Removal",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "AI-powered subject extraction works on all platforms",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Pick Image Button

        val scope = rememberCoroutineScope()
        val filePicker = rememberFilePickerLauncher(
            type = FileKitType.Image,
            onResult = { file ->
                scope.launch {
                    val imageBytes = file?.readBytes()
                    if(imageBytes != null) {
                        onEvent(EditorUiEvents.OnImageSelect(imageBytes))
                        onImagePicked(file.path)
                    }
                }
            }
        )

        when(state.modelStatus) {
            ModelStatus.NetworkError -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Internet Connection Required",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "To download the AI models for background removal, please connect to the internet.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        TextButton(
                            onClick = { onEvent(EditorUiEvents.OnDownloadModel) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            ModelStatus.GmsMissing -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Google Play Services is required for AI background removal. This device is not supported.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            ModelStatus.Unsupported -> {
                Text(
                    "This feature is not supported on iOS yet.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                Button(
                    onClick = {
                        if(state.modelStatus is ModelStatus.NotDownloaded){
                            onEvent(EditorUiEvents.OnDownloadModel)
                        } else {
                            filePicker.launch()
                        }
                    },
                    enabled = state.modelStatus !is ModelStatus.Downloading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ){
                    Text(
                        text = when (state.modelStatus) {
                            is ModelStatus.NotDownloaded -> "Download Models (Required)"
                            is ModelStatus.Downloading -> "Downloading... Please wait"
                            else -> "Pick Image"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }

            }
        }
    }
}
