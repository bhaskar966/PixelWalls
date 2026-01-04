package com.bhaskar.pixelwalls.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.bhaskar.pixelwalls.utils.editor.HalfClipShape
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.regular.ArrowLeft

@Composable
fun FullScreenEditor(
    imageUri: String,
    navController: NavHostController,
    state: EditorState
){

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        LaunchedEffect(imageUri) {
            println("imageUri: $imageUri")
        }

//        AsyncImage(
//            model = imageUri,
//            contentDescription = "Original Background",
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )

        if(state.isLoading) {
            CircularProgressIndicator()
        } else if (state.originalImageUri != null && state.subjectImageUri != null) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val imgWidth = constraints.maxWidth.toFloat()
                val imgHeight = constraints.maxHeight.toFloat()

                val subjectBoundsPx = state.subjectBounds.scale(imgWidth, imgHeight)
                val circleRadius = (subjectBoundsPx.width / 2f) * 1.2f
                val circleCenter = subjectBoundsPx.center

                Text("FullScreen Editor")

                AsyncImage(
                    model = state.originalImageUri,
                    contentDescription = "Original Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                ) {

                    drawRect(
                        color = Color(0xFF6C5728)
                    )

                    drawCircle(
                        color = Color.Transparent,
                        radius = circleRadius,
                        center = circleCenter,
                        blendMode = BlendMode.Clear
                    )
                }

                AsyncImage(
                    model = state.subjectImageUri,
                    contentDescription = "Foreground Subject",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(HalfClipShape())
                )

            }
        } else if(state.error != null) {
            println("Error, ${state.error}")
            Text("Error: ${state.error}")
        }

        Icon(
            imageVector = FluentIcons.Regular.ArrowLeft,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(start = 20.dp)
                .align(Alignment.TopStart)
                .size(40.dp)
                .clickable {
                    navController.popBackStack()
                }
        )
    }

}

private fun Rect.scale(width: Float, height: Float) =  Rect(
    left = this.left * width,
    top = this.top * height,
    right = this.right * width,
    bottom = this.bottom * height
)