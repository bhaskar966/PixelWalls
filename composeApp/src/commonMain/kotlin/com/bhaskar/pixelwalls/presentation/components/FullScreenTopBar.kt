package com.bhaskar.pixelwalls.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Checkmark
import fluent.ui.system.icons.regular.ArrowLeft

@Composable
fun FullScreenTopBar(
    onBackClick: () -> Unit,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector = FluentIcons.Filled.Checkmark,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(FluentIcons.Regular.ArrowLeft, "Back", tint = Color.White)
        }

        // Optional Action Button (Save/Tick)
        if (onActionClick != null) {
            IconButton(
                onClick = onActionClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = "Action",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
