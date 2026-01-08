package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.ControlPanel
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.allShapes
import com.bhaskar.pixelwalls.utils.input.onDesktopMouseScroll
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.regular.ArrowLeft

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FullScreenEditor(
    imageUri: String,
    navController: NavHostController,
    state: EditorState,
    onEvent: (EditorUiEvents) -> Unit,
) {

    val isControlPanelVisible by remember(state.isControlPanelVisible) {
        derivedStateOf { state.isControlPanelVisible }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            return@Box
        }

        val original = state.originalImageUri
        val subject = state.subjectImageUri

        if (original == null || subject == null) {
            state.error?.let {
                Text(
                    text = "Error: $it",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            return@Box
        }

        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onEvent(EditorUiEvents.OnControlPanelToggle)
                }
        ) {

            val containerW = constraints.maxWidth.toFloat()
            val containerH = constraints.maxHeight.toFloat()
            val minDim = minOf(containerW, containerH)


            // hollow geometry (screen space)
            val radius = (minDim * state.shapeRadiusPercent / 2f)
                .coerceAtMost((minDim * 0.9f) / 2f)

            val centerY = with(LocalDensity.current) {
                val padPx = 8.dp.toPx()
                val minY = radius + padPx
                val maxY = containerH - radius - padPx
                (minY + (maxY - minY) * state.hollowCenterYPercent).coerceIn(minY, maxY)
            }

            val center = Offset(x = containerW / 2f, y = centerY)
            val pivotX = 0.5f
            val pivotY = (center.y / containerH).coerceIn(0f, 1f)

            // Invert the clipHeightPercent so that a higher value means more of the subject is visible.
            val invertedClipPercent = 1.0f - state.clipHeightPercent
            val cutLineY = (center.y - radius + (2f * radius * invertedClipPercent))
                .coerceIn(0f, containerH)

            val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                val newScale = (state.scale * zoomChange).coerceIn(0.5f, 3f)
                val newOffsetX = state.offsetX + panChange.x
                val newOffsetY = state.offsetY + panChange.y
                onEvent(EditorUiEvents.OnScaleChange(newScale))
                onEvent(EditorUiEvents.OnOffsetChange(newOffsetX, newOffsetY))
            }

            // Resize compensation (kept as-is)
            val containerSize = remember(containerW, containerH) {
                IntSize(containerW.toInt(), containerH.toInt())
            }
            var lastContainerSize by remember { mutableStateOf(IntSize.Zero) }

            LaunchedEffect(containerSize) {
                val prev = lastContainerSize
                if (prev.width > 0 && prev.height > 0) {
                    val sx = containerSize.width.toFloat() / prev.width.toFloat()
                    val sy = containerSize.height.toFloat() / prev.height.toFloat()

                    onEvent(
                        EditorUiEvents.OnOffsetChange(
                            offsetX = state.offsetX * sx,
                            offsetY = state.offsetY * sy
                        )
                    )
                }
                lastContainerSize = containerSize
            }

            // Image transform modifier (kept logic)
            val imageTransform = Modifier
                .fillMaxSize()
                .transformable(transformableState)
                .graphicsLayer {
                    transformOrigin = TransformOrigin(pivotX, pivotY)

                    scaleX = state.scale
                    scaleY = state.scale

                    translationX = state.offsetX
                    translationY = state.offsetY
                }
                .onDesktopMouseScroll { delta ->
                    val zoomFactor = 1.1f
                    val newScale = if (delta > 0) state.scale / zoomFactor else state.scale * zoomFactor
                    onEvent(EditorUiEvents.OnScaleChange(newScale.coerceIn(0.5f, 3f)))
                }

            // hollow path (screen) + local conversions
            val density = LocalDensity.current
            val layoutDir = LocalLayoutDirection.current


            val maskShape = allShapes.find { it.first == state.shape }?.second?.toShape() ?: CircleShape
            val hollowPath = remember(radius, center, state.shape, density, layoutDir) {
                buildMaskPath(
                    shape = maskShape,
                    diameter = radius * 2f,
                    center = center,
                    density = density,
                    layoutDirection = layoutDir
                )
            }

            val s = state.scale
            val tx = state.offsetX
            val ty = state.offsetY
            val invScale = 1f / s

            val hollowPathLocal = remember(hollowPath, s, tx, ty) {
                Path().apply {
                    addPath(hollowPath)
                    transform(
                        Matrix().apply {
                            translate(-tx, -ty)
                            scale(invScale, invScale)
                        }
                    )
                }
            }

            val cutLineYLocal = ((cutLineY - ty) * invScale).coerceIn(0f, containerH)

            // LAYER 1
            Box(modifier = imageTransform) {
                AsyncImage(
                    model = original,
                    contentDescription = "Original",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                AsyncImage(
                    model = subject,
                    contentDescription = "Subject behind wall",
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            onDrawWithContent {
                                clipRect(
                                    left = 0f,
                                    top = cutLineYLocal,
                                    right = size.width,
                                    bottom = size.height
                                ) {
                                    clipPath(hollowPathLocal) {
                                        this@onDrawWithContent.drawContent()
                                    }
                                }
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }

            // LAYER 2: wall overlay
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                drawRect(color = state.bgColor)
                drawPath(
                    path = hollowPath,
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear
                )
            }


            // LAYER 3b: pop-out
            AsyncImage(
                model = subject,
                contentDescription = "Subject pop-out",
                modifier = imageTransform
                    .drawWithCache {
                        onDrawWithContent {
                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = cutLineYLocal
                            ) {
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

        ControlPanel(
            state = state,
            onShapeRadiusChange = { onEvent(EditorUiEvents.OnShapeRadiusChange(it)) },
            onClipHeightChange = { onEvent(EditorUiEvents.OnClipHeightChange(it)) },
            onHollowYChange = { onEvent(EditorUiEvents.OnHollowYChange(it)) },
            onBgColorChange = { onEvent(EditorUiEvents.OnBgColorChange(it)) },
            onShapeChange = { onEvent(EditorUiEvents.OnShapeChange(it)) },
            isControlPanelVisible = isControlPanelVisible
        )

        Icon(
            imageVector = FluentIcons.Regular.ArrowLeft,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(start = 20.dp, top = 20.dp)
                .align(Alignment.TopStart)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )
    }
}

/**
 * Creates a Path for a Shape of size (diameter x diameter) positioned so its center == `center`.
 */
private fun buildMaskPath(
    shape: Shape,
    diameter: Float,
    center: Offset,
    density: Density,
    layoutDirection: LayoutDirection
): Path {
    val outline = shape.createOutline(
        size = Size(diameter, diameter),
        layoutDirection = layoutDirection,
        density = density
    )
//
//    val localPath = outline.toPath()
//    val dx = center.x - diameter / 2f
//    val dy = center.y - diameter / 2f

    return outline.toPath().apply {
        translate(
            center - Offset(diameter/ 2f, diameter / 2f)
        )
    }
}

private fun Outline.toPath(): Path = when (this) {
    is Outline.Generic -> this.path
    is Outline.Rectangle -> Path().apply { addRect(rect) }
    is Outline.Rounded -> Path().apply { addRoundRect(roundRect) }
}
