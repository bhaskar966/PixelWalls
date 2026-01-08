package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.bhaskar.pixelwalls.domain.capture.ImageCaptureService
import com.bhaskar.pixelwalls.domain.capture.ImageFormat
import com.bhaskar.pixelwalls.domain.capture.ImageSaveService
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.ControlPanel
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.allShapes
import com.bhaskar.pixelwalls.utils.input.onDesktopMouseScroll
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Checkmark
import fluent.ui.system.icons.regular.ArrowLeft
import fluent.ui.system.icons.regular.Save
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
@Composable
fun FullScreenEditor(
    imageUri: String,
    navController: NavHostController,
    state: EditorState,
    onEvent: (EditorUiEvents) -> Unit,
) {
    val captureService = koinInject<ImageCaptureService>()
    val saveService = koinInject<ImageSaveService>()
    val scope = rememberCoroutineScope()

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        onEvent(EditorUiEvents.OnScaleChange((state.scale * zoomChange).coerceIn(0.5f, 5f)))
        onEvent(EditorUiEvents.OnOffsetChange(state.offsetX + panChange.x, state.offsetY + panChange.y))
    }

    val capturableCanvas = captureService.rememberCapturableContent {
        EditorCanvasOnly(state = state)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformableState)
                .onDesktopMouseScroll { delta ->
                    val zoomFactor = 1.1f
                    val newScale = if (delta > 0) state.scale / zoomFactor else state.scale * zoomFactor
                    onEvent(EditorUiEvents.OnScaleChange(newScale.coerceIn(0.5f, 5f)))
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onEvent(EditorUiEvents.OnControlPanelToggle)
                }
        ) {
            capturableCanvas.composable()
        }


        AnimatedVisibility(
            visible = state.isControlPanelVisible,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (state.originalBitmap != null) {
                ControlPanel(
                    state = state,
                    originalImageBitmap = state.originalBitmap,
                    onShapeRadiusChange = { onEvent(EditorUiEvents.OnShapeRadiusChange(it)) },
                    onClipHeightChange = { onEvent(EditorUiEvents.OnClipHeightChange(it)) },
                    onHollowYChange = { onEvent(EditorUiEvents.OnHollowYChange(it)) },
                    onBgColorChange = { onEvent(EditorUiEvents.OnBgColorChange(it)) },
                    onShapeChange = { onEvent(EditorUiEvents.OnShapeChange(it)) },
                    onColorPickerVisibilityChanged = { onEvent(EditorUiEvents.OnColorPickerToggle(it)) },
                    isControlPanelVisible = state.isControlPanelVisible
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = FluentIcons.Regular.ArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    scope.launch {
                        // Hide UI for a clean capture
                        onEvent(EditorUiEvents.OnControlPanelToggle)
                        delay(150)

                        capturableCanvas.capture()
                            .onSuccess { bytes ->
                                saveService.saveToGallery(
                                    fileName = "PixelWall_${Clock.System.now().toEpochMilliseconds()}",
                                    imageBytes = bytes,
                                    format = ImageFormat.PNG
                                ).onSuccess {
                                    println("Export Successful: $it")
                                }
                            }
                        // Show UI again
                        onEvent(EditorUiEvents.OnControlPanelToggle)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = FluentIcons.Filled.Checkmark,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditorCanvasOnly(state: EditorState) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerW = constraints.maxWidth.toFloat()
        val containerH = constraints.maxHeight.toFloat()
        val minDim = minOf(containerW, containerH)
        val density = LocalDensity.current
        val layoutDir = LocalLayoutDirection.current

        // 1. Calculate Shape Geometry
        val radius = (minDim * state.shapeRadiusPercent / 2f).coerceAtMost((minDim * 0.9f) / 2f)
        val centerY = with(density) {
            val padPx = 8.dp.toPx()
            val minY = radius + padPx
            val maxY = containerH - radius - padPx
            (minY + (maxY - minY) * state.hollowCenterYPercent).coerceIn(minY, maxY)
        }
        val center = Offset(x = containerW / 2f, y = centerY)

        // Pivot for zooming (center of the shape)
        val pivotX = 0.5f
        val pivotY = (center.y / containerH).coerceIn(0f, 1f)

        // 2. Clipping Logic
        val invertedClipPercent = 1.0f - state.clipHeightPercent
        val cutLineY = (center.y - radius + (2f * radius * invertedClipPercent)).coerceIn(0f, containerH)

        // Transform for background and subject
        val s = state.scale
        val tx = state.offsetX
        val ty = state.offsetY
        val invScale = 1f / s

        val imageTransform = Modifier
            .fillMaxSize()
            .graphicsLayer {
                transformOrigin = TransformOrigin(pivotX, pivotY)
                scaleX = s
                scaleY = s
                translationX = tx
                translationY = ty
            }

        // 3. Path Logic
        val maskShape = allShapes.find { it.first == state.shape }?.second?.toShape() ?: CircleShape
        val hollowPath = remember(radius, center, state.shape, density, layoutDir) {
            buildMaskPath(maskShape, radius * 2f, center, density, layoutDir)
        }

        // Transform the path to local image space so the hole stays "fixed" to the screen
        // while the image content moves behind it.
        val hollowPathLocal = remember(hollowPath, s, tx, ty) {
            Path().apply {
                addPath(hollowPath)
                transform(Matrix().apply {
                    translate(-tx, -ty)
                    scale(invScale, invScale)
                })
            }
        }
        val cutLineYLocal = ((cutLineY - ty) * invScale).coerceIn(0f, containerH)

        // LAYER 1: Background
        Box(modifier = imageTransform) {
            AsyncImage(
                model = state.originalImageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // LAYER 3a: Subject (Behind the wall hole)
            AsyncImage(
                model = state.subjectImageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        onDrawWithContent {
                            clipRect(top = cutLineYLocal) {
                                clipPath(hollowPathLocal) {
                                    this@onDrawWithContent.drawContent()
                                }
                            }
                        }
                    },
                contentScale = ContentScale.Fit
            )
        }

        // LAYER 2: The Color Wall
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }) {
            drawRect(color = state.bgColor)
            drawPath(path = hollowPath, color = Color.Transparent, blendMode = BlendMode.Clear)
        }

        // LAYER 3b: Subject Pop-out (In front of the wall)
        AsyncImage(
            model = state.subjectImageUri,
            contentDescription = null,
            modifier = imageTransform
                .drawWithCache {
                    onDrawWithContent {
                        clipRect(bottom = cutLineYLocal) {
                            this@onDrawWithContent.drawContent()
                        }
                    }
                }
                .graphicsLayer {
                    rotationX = -10f
                    cameraDistance = 24f * density.density
                },
            contentScale = ContentScale.Fit
        )
    }
}

private fun buildMaskPath(
    shape: Shape,
    diameter: Float,
    center: Offset,
    density: Density,
    layoutDirection: LayoutDirection
): Path {
    val outline = shape.createOutline(Size(diameter, diameter), layoutDirection, density)
    return outline.toPath().apply {
        translate(center - Offset(diameter / 2f, diameter / 2f))
    }
}

private fun Outline.toPath(): Path = when (this) {
    is Outline.Generic -> this.path
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
}
