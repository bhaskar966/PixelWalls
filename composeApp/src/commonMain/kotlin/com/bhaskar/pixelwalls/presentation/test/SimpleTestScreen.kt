package com.bhaskar.pixelwalls.presentation.test

import com.bhaskar.pixelwalls.backgroundremoval.toImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.backgroundremoval.createBackgroundRemover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pixelwalls.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SimpleTestScreen() {
    var originalImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var processedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var processingTime by remember { mutableStateOf<Long?>(null) }

    val backgroundRemover = remember { createBackgroundRemover() }
    val scope = rememberCoroutineScope()

    // Store original image bytes for reprocessing
    var originalBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Load test image on first composition
    LaunchedEffect(Unit) {
        try {
            // Load image bytes directly from resources
            val imageBytes = Res.readBytes("drawable/test_image.jpg")
            originalBytes = imageBytes

            // Convert to ImageBitmap for display (using our extension)
            originalImage = imageBytes.toImageBitmap()
        } catch (e: Exception) {
            error = "Failed to load image: ${e.message}"
            e.printStackTrace()
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            backgroundRemover.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background Removal Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Original Image Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Original Image",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            originalImage != null -> {
                                Image(
                                    bitmap = originalImage!!,
                                    contentDescription = "Original",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            error == null -> {
                                CircularProgressIndicator()
                            }
                            else -> {
                                Text("Failed to load")
                            }
                        }
                    }
                }
            }

            // Process Button
            Button(
                onClick = {
                    scope.launch {
                        isProcessing = true
                        error = null
                        processedImage = null
                        processingTime = null

                        try {
                            val imageBytes = originalBytes
                                ?: throw IllegalStateException("No image loaded")

                            // Process with your BackgroundRemover
                            val result = withContext(Dispatchers.Default) {
                                backgroundRemover.removeBackground(imageBytes)
                            }

                            result.onSuccess { processed ->
                                // Convert processed bytes to ImageBitmap
                                processedImage = processed.imageBytes.toImageBitmap()
                                processingTime = processed.processingTimeMs
                            }.onFailure { e ->
                                error = "Processing failed: ${e.message}"
                                e.printStackTrace()
                            }
                        } catch (e: Exception) {
                            error = "Processing failed: ${e.message}"
                            e.printStackTrace()
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                enabled = !isProcessing && originalImage != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isProcessing) "Processing..." else "Remove Background")
            }

            // Processing Time
            if (processingTime != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "⏱️ Processing time: ${processingTime}ms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Processed Image Card
            if (processedImage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Processed Image (Background Removed)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = processedImage!!,
                                contentDescription = "Processed",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Error Message
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "❌ Error",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
