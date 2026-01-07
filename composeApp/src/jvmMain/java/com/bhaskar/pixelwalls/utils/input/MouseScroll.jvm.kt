package com.bhaskar.pixelwalls.utils.input

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
@Suppress(names = ["NO_ACTUAL_FOR_EXPECT"])
actual fun Modifier.onDesktopMouseScroll(onScroll: (delta: Float) -> Unit): Modifier {
    return this.onPointerEvent(PointerEventType.Scroll) {
        val delta = it.changes.first().scrollDelta.y
        onScroll(delta)
    }
}