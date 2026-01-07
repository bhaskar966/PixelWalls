package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.presentation.editor.EditorState

@Composable
fun ControlPanel(
    state: EditorState,
    onShapeRadiusChange: (Float) -> Unit,
    onClipHeightChange: (Float) -> Unit,
    onHollowYChange: (Float) -> Unit,
    onBgColorChange: (Color) -> Unit,
    onShapeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var currentPage by remember { mutableStateOf(ControlPage.ADJUST) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 16.dp, horizontal = 10.dp), // Use more vertical padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                when(currentPage) {
                    ControlPage.ADJUST -> {
                        AdjustPanel(
                            state = state,
                            onShapeRadiusChange = onShapeRadiusChange,
                            onClipHeightChange = onClipHeightChange,
                            onHoleYChange = onHollowYChange
                        )
                    }
                    ControlPage.BACKGROUND -> {
                        BackgroundPanel(
                            selectedColor = state.bgColor,
                            onColorSelected = { color ->
                                onBgColorChange(color)
                            }
                        )
                    }
                    ControlPage.SHAPE -> {
                        ShapePanel(
                            selectedShapeName = state.shape,
                            onShapeSelected = { shapeName ->
                                onShapeChange(shapeName)
                            }
                        )
                    }
                }

            }

            ControlPanelNavigation(
                selectedPage = currentPage,
                onPageSelected = { newPage ->
                    currentPage = newPage
                }
            )

        }
    }

}
