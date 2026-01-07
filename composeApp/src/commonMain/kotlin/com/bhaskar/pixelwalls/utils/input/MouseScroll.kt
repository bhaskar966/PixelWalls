package com.bhaskar.pixelwalls.utils.input

import androidx.compose.ui.Modifier

/**
 * Expects a platform-specific modifier to handle mouse scroll events for zooming.
 *
 * @param onScroll A lambda function that is invoked when a scroll event occurs.
 *                 It receives the scroll delta (negative for zoom in, positive for zoom out).
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun Modifier.onDesktopMouseScroll(
    onScroll: (delta: Float) -> Unit
): Modifier