package com.bhaskar.pixelwalls.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bhaskar.pixelwalls.presentation.editor.EditorScreenViewModel
import com.bhaskar.pixelwalls.presentation.editor.FullScreenEditor
import com.bhaskar.pixelwalls.presentation.main.MainScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {

    val rootNavController = rememberNavController()
    val editorViewModel: EditorScreenViewModel = koinViewModel()
    val state by editorViewModel.editorUiState.collectAsState()
    val editorUiEvents = editorViewModel::onEvent


    NavHost(
        navController = rootNavController,
        startDestination = RootNavGraph.MainScreen
    ) {

        composable<RootNavGraph.MainScreen>() {

            MainScreen(
                rootNavController = rootNavController,
                editorUiEvents = editorUiEvents,
                editorState = state,
            )

        }

        composable<RootNavGraph.FullScreenEditorScreen>() { navBackStackEntry ->
            FullScreenEditor(
                imageUri = navBackStackEntry.toRoute<RootNavGraph.FullScreenEditorScreen>().imageUri,
                navController = rootNavController,
                state = state,
                onEvent = editorUiEvents
            )
        }

    }

}