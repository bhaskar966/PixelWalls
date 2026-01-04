package com.bhaskar.pixelwalls

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.bhaskar.pixelwalls.di.initKoin

fun main() = application {


    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "PixelWalls",
    ) {
        App()
    }
}