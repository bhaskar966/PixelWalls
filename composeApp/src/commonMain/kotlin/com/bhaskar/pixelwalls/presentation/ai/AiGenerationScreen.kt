package com.bhaskar.pixelwalls.presentation.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.bhaskar.pixelwalls.presentation.ai.ui_components.AspectRatioSelector
import com.bhaskar.pixelwalls.presentation.ai.ui_components.CategoryDropdown
import com.bhaskar.pixelwalls.presentation.ai.ui_components.PromptText
import com.bhaskar.pixelwalls.presentation.ai.ui_components.VariableOptionsGrid
import com.bhaskar.pixelwalls.presentation.components.WallpaperActionsDialog
import com.bhaskar.pixelwalls.utils.NebulaWaveOverlay
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.regular.Add
import fluent.ui.system.icons.regular.ArrowClockwise
import fluent.ui.system.icons.regular.ArrowLeft
import fluent.ui.system.icons.regular.Checkmark
import fluent.ui.system.icons.regular.Lightbulb

@Composable
fun AiGenerationScreen(
    state: AiUiState,
    onEvent: (AiUiEvents) -> Unit,
    navController: NavHostController
) {

    val containerSize = LocalWindowInfo.current.containerSize

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isGenerating -> {
                NebulaWaveOverlay()
            }

            state.generatedImageBytes != null -> {
                AsyncImage(
                    model = state.generatedImageBytes,
                    contentDescription = "AI Generated",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceTint
                                ),
                                center = Offset(
                                    x = containerSize.width / 2f,
                                    y = containerSize.height.toFloat()
                                ),
                                radius = containerSize.width.toFloat()
                            )
                        )

                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    RoundedCornerShape(50)
                )
            ) {
                Icon(
                    imageVector = FluentIcons.Regular.ArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            CategoryDropdown(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { onEvent(AiUiEvents.SelectCategory(it)) }
            )

            if (state.generatedImageBytes != null) {
                IconButton(
                    onClick = { onEvent(AiUiEvents.OnActionClick) },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = FluentIcons.Regular.Checkmark,
                        contentDescription = "Apply",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                AnimatedContent(
                    targetState = state.editingVariable,
                    transitionSpec = {
                        (fadeIn() + slideInVertically { it / 2 })
                            .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                    },
                    label = "PromptTransition"
                ) { editingVariable ->
                    if (editingVariable != null) {
                        VariableOptionsGrid(
                            variable = state.selectedTemplate.variables[editingVariable]!!,
                            selectedIndex = state.promptSelection[editingVariable] ?: 0,
                            onOptionSelected = { index ->
                                onEvent(AiUiEvents.SelectVariableOption(editingVariable, index))
                            },
                            onClose = { onEvent(AiUiEvents.CloseVariableEditor) }
                        )
                    } else {
                        Column {
                            PromptText(
                                template = state.selectedTemplate,
                                selection = state.promptSelection,
                                onVariableClick = { variableKey ->
                                    onEvent(AiUiEvents.OpenVariableEditor(variableKey))
                                }
                            )

                            Spacer(Modifier.height(24.dp))

                            AspectRatioSelector(
                                selectedRatio = state.aspectRatio,
                                onRatioSelected = { onEvent(AiUiEvents.OnUpdateAspectRatio(it)) },
                                isVisible = state.isAspectRatioSelectorVisible,
                                onToggle = { onEvent(AiUiEvents.OnToggleAspectRatioSelector) }
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onEvent(AiUiEvents.RandomizePrompt) },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Icon(
                                        imageVector = FluentIcons.Regular.ArrowClockwise,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Surprise Me", style = MaterialTheme.typography.labelLarge)
                                }

                                Button(
                                    onClick = {
                                        onEvent(AiUiEvents.Generate)
                                  },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    enabled = !state.isGenerating
                                ) {
                                    if (state.isGenerating) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = FluentIcons.Regular.Lightbulb,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text =  "Create",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.error != null) {
                    Text(
                        state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                }
            }
        }

        if (state.showWallpaperDialog && state.generatedImageBytes != null) {
            WallpaperActionsDialog(
                showSaveToGallery = false,
                isOperating = state.isOperating,
                currentStep = state.currentActionStep,
                wallpaperResult = state.wallpaperResult,
                isShareSupported = state.isShareSupported,
                onDismiss = { onEvent(AiUiEvents.OnDismissDialog) },
                onSetWallpaper = { onEvent(AiUiEvents.OnSetWallpaper(it)) },
                onShare = { onEvent(AiUiEvents.OnShareClick) },
                onLocate = { onEvent(AiUiEvents.OnLocateClick) }
            )
        }
    }
}