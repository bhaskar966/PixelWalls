package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhaskar.pixelwalls.presentation.editor.EditorState

@Composable
fun AdjustPanel(
    state: EditorState,
    onShapeRadiusChange: (Float) -> Unit,
    onClipHeightChange: (Float) -> Unit,
    onHoleYChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ControlSliders(
            value = state.shapeRadiusPercent,
            onValueChange = onShapeRadiusChange,
            valueRange = 0.3f..0.9f,
            labelText = "Shape Size",
            percentageText = (state.shapeRadiusPercent * 100).toInt().toString()
        )

        ControlSliders(
            value = state.clipHeightPercent,
            onValueChange = onClipHeightChange,
            valueRange = 0.3f..1f,
            labelText = "Clip Height",
            percentageText = (state.clipHeightPercent * 100).toInt().toString()
        )


        ControlSliders(
            value = state.hollowCenterYPercent,
            onValueChange = onHoleYChange,
            valueRange = 0f..1f,
            labelText = "Hole Y",
            percentageText = (state.hollowCenterYPercent * 100).toInt().toString()
        )

        Text(
            text = "Pinch to zoom • Drag to reposition",
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}


@Composable
fun ControlSliders(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    labelText: String,
    percentageText: String
){

    Column {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = labelText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$percentageText%",
                fontSize = 14.sp
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                activeTrackColor = MaterialTheme.colorScheme.onPrimaryContainer,
                inactiveTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
            )
        )
    }


}