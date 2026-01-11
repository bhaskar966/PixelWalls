package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.domain.service.ModelStatus
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import pixelwalls.composeapp.generated.resources.Res
import pixelwalls.composeapp.generated.resources.pixelwalls_popout_effect_demo

@Composable
fun EditorScreen(
    onImagePicked: (String) -> Unit,
    state: EditorState,
    onEvent: (EditorUiEvents) -> Unit
) {
    val scope = rememberCoroutineScope()
    val filePicker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = { file ->
            scope.launch {
                val imageBytes = file?.readBytes()
                if (imageBytes != null) {
                    onEvent(EditorUiEvents.OnImageSelect(imageBytes))
                    onImagePicked(file.path)
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(32.dp))

            Image(
                painter = painterResource(Res.drawable.pixelwalls_popout_effect_demo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Create 3D Pop-out Wallpapers",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Make people or subjects emerge from your screen with Material You depth and shape effects.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            when (state.modelStatus) {
                ModelStatus.Unsupported -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "3D Pop-out effects are not supported on iOS yet. We're working on it!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ModelStatus.GmsMissing -> {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Google Play Services is required for AI background removal. This device is not supported.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                ModelStatus.NetworkError -> {
                    Button(
                        onClick = { onEvent(EditorUiEvents.OnDownloadModel) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Retry Downloading AI Models")
                    }
                }

                else -> {
                    Button(
                        onClick = {
                            if (state.modelStatus is ModelStatus.NotDownloaded) {
                                onEvent(EditorUiEvents.OnDownloadModel)
                            } else {
                                filePicker.launch()
                            }
                        },
                        enabled = state.modelStatus !is ModelStatus.Downloading,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.modelStatus is ModelStatus.Downloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(
                            text = when (state.modelStatus) {
                                is ModelStatus.NotDownloaded -> "Download AI Models"
                                is ModelStatus.Downloading -> "Downloading..."
                                else -> "Pick Image to Start"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
