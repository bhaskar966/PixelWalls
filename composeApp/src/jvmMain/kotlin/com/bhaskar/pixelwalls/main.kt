package com.bhaskar.pixelwalls

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.bhaskar.pixelwalls.di.initKoin
import java.awt.Dimension
import java.awt.Toolkit

fun main() = application {

    initKoin()

    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val screenHeight = screenSize.height

    val targetHeightPx = screenHeight * 0.7f
    val targetWidthPx = targetHeightPx * (16f/9f)

    val windowState = rememberWindowState(
        size = DpSize(
            width = targetWidthPx.toInt().dp,
            height = targetHeightPx.toInt().dp
        )
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "PixelWalls",
        resizable = true
    ) {
        window.minimumSize = Dimension(800, 450)
        App()
    }
}