package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.allShapes


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShapePanel(
    selectedShapeName: String,
    onShapeSelected: (String) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .height(200.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(allShapes) { (name, shapeProperty) ->

            val shape = shapeProperty.toShape()
            val isSelected = selectedShapeName == name

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .aspectRatio(1f)
                    .clip(
                        if (isSelected) RoundedCornerShape(10.dp) else CircleShape
                    )
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                        .aspectRatio(1f)
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onShapeSelected(name)
                        }
                )
            }
        }
    }

}