package com.bhaskar.pixelwalls.presentation.navigation

import kotlinx.serialization.Serializable

sealed class BottomNavGraph {

    @Serializable
    data object EditorScreen: BottomNavGraph()

    @Serializable
    data object AIScreen: BottomNavGraph()

    @Serializable
    data object CreationsScreen: BottomNavGraph()

}

sealed class RootNavGraph {

    @Serializable
    data object MainScreen: RootNavGraph()

    @Serializable
    data class FullScreenEditorScreen(val imageUri: String): RootNavGraph()

    @Serializable
    data class CreationPreviewScreen(val imagePath: String): RootNavGraph()
}
