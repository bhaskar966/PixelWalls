package com.bhaskar.pixelwalls.utils.navigationComps

import coil3.Uri

sealed class EditorScreenEvent {
    data class OnImagePicked(val imageUri: String) : EditorScreenEvent()

}