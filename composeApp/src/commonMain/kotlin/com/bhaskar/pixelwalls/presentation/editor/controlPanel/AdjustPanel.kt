package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhaskar.pixelwalls.presentation.editor.EditorState
import com.bhaskar.pixelwalls.utils.zoomInstructionText

@Composable
fun AdjustPanel(
    state: EditorState,
    onShapeRadiusChange: (Float) -> Unit,
    onClipHeightChange: (Float) -> Unit,
    onHoleYChange: (Float) -> Unit,
    onSubjectToggle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "3D Pop-out Effect",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = state.isSubjectEnabled,
                onCheckedChange = onSubjectToggle,
            )
        }

        ControlSliders(
            value = state.shapeRadiusPercent,
            onValueChange = onShapeRadiusChange,
            valueRange = 0.3f..0.9f,
            labelText = "Shape Size",
            percentageText = (state.shapeRadiusPercent * 100).toInt().toString()
        )

        if (state.isSubjectEnabled) {
            ControlSliders(
                value = state.clipHeightPercent,
                onValueChange = onClipHeightChange,
                valueRange = 0.3f..1f,
                labelText = "Pop-out Amount", // More intuitive name
                percentageText = (state.clipHeightPercent * 100).toInt().toString()
            )
        }


        ControlSliders(
            value = state.hollowCenterYPercent,
            onValueChange = onHoleYChange,
            valueRange = 0f..1f,
            labelText = "Hole Y",
            percentageText = (state.hollowCenterYPercent * 100).toInt().toString()
        )

        Text(
            text = zoomInstructionText,
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