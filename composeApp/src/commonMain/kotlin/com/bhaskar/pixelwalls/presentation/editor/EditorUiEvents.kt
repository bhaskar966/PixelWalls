package com.bhaskar.pixelwalls.presentation.editor

sealed class EditorUiEvents {

    data class OnImageSelect(val imageBytes: ByteArray): EditorUiEvents()

}