package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.oppositeColor
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.ImageColorPicker
import com.github.skydoves.colorpicker.compose.PaletteContentScale
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Drop

@Composable
fun BackgroundPanel(
    originalImageBitmap: ImageBitmap,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    isColorPickerVisible: Boolean,
    onColorPickerVisibilityChanged: (Boolean) -> Unit
) {
    val presetColors = listOf(
        Color(0xFFE6B34A), Color(0xFFD4C7A8), Color(0xFFC0A66E), Color(0xFF3A5969),
        Color(0xFF88A0A8), Color(0xFF384030), Color(0xFF8D8370), Color(0xFF6A4C4C),
        Color(0xFF3D9A9A)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {

            items(presetColors) { color ->

                val isSelected = selectedColor == color

                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isSelected) Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(16.dp)
                                )
                                else Modifier
                            )
                            .clickable { onColorSelected(color) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(if (isSelected) RoundedCornerShape(10.dp) else CircleShape)
                                .background(color)
                        )
                    }
                }

            }

            item {
                val isCustomColorApplied = selectedColor !in presetColors.toSet()

                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isCustomColorApplied) Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(16.dp)
                                )
                                else Modifier
                            )
                            .clickable {
                                onColorPickerVisibilityChanged(true)
                            }
                    ) {
                        val boxBgCol = if (isCustomColorApplied) selectedColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(if (isCustomColorApplied) RoundedCornerShape(10.dp) else CircleShape)
                                .background(boxBgCol),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FluentIcons.Filled.Drop,
                                contentDescription = "Choose Custom Color",
                                tint = if (isCustomColorApplied) boxBgCol.oppositeColor() else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

        }


        AnimatedVisibility(
            visible = isColorPickerVisible
        ) {
            ColorPickerPopUp(
                imageBitmap = originalImageBitmap,
                selectedColor = selectedColor,
                onDismissRequest = {
                    onColorPickerVisibilityChanged(false)
                },
                onColorSelected = { color ->
                    onColorSelected(color)
                }
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerPopUp(
    imageBitmap: ImageBitmap,
    selectedColor: Color = Color.Unspecified,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {

    val colorPickerController = rememberColorPickerController()
    val currColor = remember { mutableStateOf(Color.Unspecified) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Wheel", "Image")


    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(30.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {

                Text(
                    text = "Pick a color",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp),
                    maxLines = 1
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        TabChip(
                            text = title,
                            isSelected = isSelected,
                            onClick = {
                                selectedTabIndex = index
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    when (selectedTabIndex) {
                        0 -> {
                            HsvColorPicker(
                                controller = colorPickerController,
                                onColorChanged = { colorEnvelope ->
                                    currColor.value = colorEnvelope.color
                                },
                                initialColor = selectedColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(10.dp),
                            )
                        }

                        1 -> {

                            ImageColorPicker(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                controller = colorPickerController,
                                paletteImageBitmap = imageBitmap,
                                paletteContentScale = PaletteContentScale.FIT,
                                onColorChanged = { colorEnvelope ->
                                    currColor.value = colorEnvelope.color
                                }
                            )
                        }

                    }

                }

                BrightnessSlider(
                    controller = colorPickerController,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(35.dp),
                )

                Button(
                    onClick = {
                        onColorSelected(currColor.value)
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currColor.value,
                        contentColor = currColor.value.oppositeColor()
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Apply this")
                }
            }
        }
    }
}


@Composable
private fun TabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(40.dp),
        shape = CircleShape, // Makes it a pill shape automatically with the height
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}