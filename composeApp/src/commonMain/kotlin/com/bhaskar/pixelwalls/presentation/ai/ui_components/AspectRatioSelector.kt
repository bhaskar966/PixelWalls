package com.bhaskar.pixelwalls.presentation.ai.ui_components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.bhaskar.pixelwalls.domain.model.AiAspectRatio

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AspectRatioSelector(
    selectedRatio: AiAspectRatio,
    onRatioSelected: (AiAspectRatio) -> Unit,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AiAspectRatio.entries.forEach { ratio ->
                        val isSelected = ratio == selectedRatio
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onRatioSelected(ratio) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            RatioIcon(ratio, isSelected)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = ratio.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        OutlinedCard(
            onClick = onToggle,
            shape = RoundedCornerShape(50),
            modifier = Modifier.height(40.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp).fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatioIcon(selectedRatio, false)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = selectedRatio.label,
                    style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun RatioIcon(ratio: AiAspectRatio, isSelected: Boolean) {
    val color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(when(ratio) {
                AiAspectRatio.PORTRAIT -> 12.dp to 18.dp
                AiAspectRatio.LANDSCAPE -> 18.dp to 12.dp
                AiAspectRatio.SQUARE -> 14.dp to 14.dp
            }.let { DpSize(it.first, it.second) })
            .border(1.5.dp, color, RoundedCornerShape(2.dp))
    )
}
