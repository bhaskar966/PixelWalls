package com.bhaskar.pixelwalls.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bhaskar.pixelwalls.presentation.ai.AiGenerationScreen
import com.bhaskar.pixelwalls.presentation.ai.AiViewModel
import com.bhaskar.pixelwalls.presentation.creations.CreationPreviewScreen
import com.bhaskar.pixelwalls.presentation.creations.CreationsViewModel
import com.bhaskar.pixelwalls.presentation.editor.EditorScreenViewModel
import com.bhaskar.pixelwalls.presentation.editor.FullScreenEditor
import com.bhaskar.pixelwalls.presentation.main.MainScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {

    val rootNavController = rememberNavController()

    val editorViewModel: EditorScreenViewModel = koinViewModel()
    val editorUiState by editorViewModel.editorUiState.collectAsState()
    val editorUiEvents = editorViewModel::onEvent

    val creationsViewModel: CreationsViewModel = koinViewModel()
    val creationsUiState by creationsViewModel.creationsUiState.collectAsState()
    val creationsUiEvents = creationsViewModel::onEvent

    val aiViewModel: AiViewModel = koinViewModel()
    val aiUiState by aiViewModel.aiScreenUiState.collectAsState()
    val aiUiEvents = aiViewModel::onEvent


    NavHost(
        navController = rootNavController,
        startDestination = RootNavGraph.MainScreen
    ) {

        composable<RootNavGraph.MainScreen>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) {

            MainScreen(
                rootNavController = rootNavController,
                editorUiEvents = editorUiEvents,
                editorState = editorUiState,
                aiUiState = aiUiState
            )

        }

        composable<RootNavGraph.FullScreenEditorScreen>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) { navBackStackEntry ->
            FullScreenEditor(
                imageUri = navBackStackEntry.toRoute<RootNavGraph.FullScreenEditorScreen>().imageUri,
                navController = rootNavController,
                state = editorUiState,
                onEvent = editorUiEvents
            )
        }

        composable<RootNavGraph.CreationPreviewScreen>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RootNavGraph.CreationPreviewScreen>()
            CreationPreviewScreen(
                imagePath = route.imagePath,
                navController = rootNavController,
                state = creationsUiState,
                onEvent = creationsUiEvents
            )
        }

        composable<RootNavGraph.AiGenerationScreen>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) {
            AiGenerationScreen(
                state = aiUiState,
                onEvent = aiUiEvents,
                navController = rootNavController
            )
        }

    }

}