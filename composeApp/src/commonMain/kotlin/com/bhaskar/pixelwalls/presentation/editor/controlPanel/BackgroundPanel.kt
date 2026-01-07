package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Add

@Composable
fun BackgroundPanel(
    selectedColor: Color,
    onColorSelected: (Color)-> Unit
) {
    val presetColors = listOf(
        Color(0xFFE6B34A), Color(0xFFD4C7A8), Color(0xFFC0A66E), Color(0xFF3A5969),
        Color(0xFF88A0A8), Color(0xFF384030), Color(0xFF8D8370), Color(0xFF6A4C4C),
        Color(0xFF3D9A9A)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(8.dp)
    ) {

        items(presetColors) { color ->

            val isSelected = selectedColor == color

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)) // Use a slightly larger corner radius for the container
                    .then(
                        if (isSelected) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        ) else Modifier
                    )
                    .clickable {
                        onColorSelected(color)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(
                            if (isSelected) RoundedCornerShape(10.dp)
                            else CircleShape
                        )
                        .background(color)
                )
            }
        }

        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { /* TODO: Launch Color Picker Dialog */ }
            ) {
                Icon(
                    imageVector = FluentIcons.Filled.Add,
                    contentDescription = "Choose Custom Color",
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

    }
}