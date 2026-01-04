package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.Uri
import coil3.toUri
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
//                file?.path?.let { onImagePicked(it) }
                scope.launch {
                    val imageBytes = file?.readBytes()
                    if(imageBytes != null) {
                        onEvent(EditorUiEvents.OnImageSelect(imageBytes))
                        onImagePicked(file.path)
                    }
                }
            }
        )

        Button(
            onClick = {
                // TODO: Open image picker, then navigate
                filePicker.launch()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                "Pick Image",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
