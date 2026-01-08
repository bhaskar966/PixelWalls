package com.bhaskar.pixelwalls.presentation.editor.controlPanel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhaskar.pixelwalls.presentation.editor.controlPanel.components.Controls
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Color
import fluent.ui.system.icons.filled.Ruler
import fluent.ui.system.icons.filled.ShapeUnion
import fluent.ui.system.icons.regular.Color
import fluent.ui.system.icons.regular.Ruler
import fluent.ui.system.icons.regular.ShapeUnion


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ControlPanelNavigation(
    selectedPage: ControlPage,
    onPageSelected: (ControlPage) -> Unit
){

    val pages = listOf(
        Controls(
            label = "Adjust",
            page = ControlPage.ADJUST,
            selectedIcon = FluentIcons.Filled.Ruler,
            unSelectedIcon = FluentIcons.Regular.Ruler
        ),
        Controls(label = "Background", page = ControlPage.BACKGROUND, selectedIcon = FluentIcons.Filled.Color, unSelectedIcon = FluentIcons.Regular.Color),
        Controls(label = "Shape", page = ControlPage.SHAPE, selectedIcon = FluentIcons.Filled.ShapeUnion, unSelectedIcon = FluentIcons.Regular.ShapeUnion),

        )

    HorizontalFloatingToolbar(
        expanded = true,
        colors = FloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.surface,
            toolbarContentColor = MaterialTheme.colorScheme.onSurface,
            fabContentColor = MaterialTheme.colorScheme.surface,
            fabContainerColor = MaterialTheme.colorScheme.onSurface,
        )
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            pages.forEach { control ->

                val isSelected = selectedPage == control.page

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(
                            color = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Transparent,
                            shape = ButtonDefaults.shape
                        )
                        .clickable {
                            onPageSelected(control.page)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ){
                    AnimatedVisibility(
                        visible = isSelected
                    ) {
                        Icon(
                            imageVector = control.selectedIcon,
                            contentDescription = control.label,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }

                    Text(
                        text = control.label,
                        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }

}