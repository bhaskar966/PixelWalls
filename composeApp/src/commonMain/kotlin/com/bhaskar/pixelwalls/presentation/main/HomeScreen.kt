package com.bhaskar.pixelwalls.presentation.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bhaskar.pixelwalls.presentation.ai.AIScreen
import com.bhaskar.pixelwalls.presentation.creations.CreationsScreen
import com.bhaskar.pixelwalls.presentation.editor.EditorScreen
import com.bhaskar.pixelwalls.utils.navigationComps.BottomNavItem
import com.bhaskar.pixelwalls.utils.navigationComps.SurfaceDestinationRoutes
import fluent.ui.system.icons.FluentIcons
import fluent.ui.system.icons.filled.Edit
import fluent.ui.system.icons.filled.Folder
import fluent.ui.system.icons.filled.Image
import fluent.ui.system.icons.regular.Edit
import fluent.ui.system.icons.regular.Folder
import fluent.ui.system.icons.regular.Image

@Composable
fun HomeScreen(){

    val selectedItem = rememberSaveable {
        mutableIntStateOf(0)
    }

    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedItem = selectedItem,
                bottomNavController = bottomNavController
            )
        },
        content = {
            BottomNavScreen(
                bottomNavController = bottomNavController
            )
        }
    )
}

@Composable
fun BottomNavScreen(
    bottomNavController: NavHostController
){

    NavHost(
        navController = bottomNavController,
        startDestination = SurfaceDestinationRoutes.EditorScreen
    ) {
        composable<SurfaceDestinationRoutes.EditorScreen>() {
            EditorScreen()
        }

        composable<SurfaceDestinationRoutes.AIScreen>() {
            AIScreen()
        }

        composable<SurfaceDestinationRoutes.CreationsScreen>() {
            CreationsScreen()
        }
    }

}

@Composable
fun BottomNavBar(
    selectedItem: MutableIntState,
    bottomNavController: NavHostController
) {

    val items = listOf(
        BottomNavItem(
            title = "Editor",
            selectedIcon = FluentIcons.Filled.Edit,
            unselectedIcon = FluentIcons.Regular.Edit
        ),
        BottomNavItem(
            title = "AI",
            selectedIcon = FluentIcons.Filled.Image,
            unselectedIcon = FluentIcons.Regular.Image
        ),
        BottomNavItem(
            title = "Creations",
            selectedIcon = FluentIcons.Filled.Folder,
            unselectedIcon = FluentIcons.Regular.Folder
        )
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedItem.intValue == index,
                onClick = {
                    selectedItem.intValue = index
                    when(index) {
                        0 -> bottomNavController.navigate(SurfaceDestinationRoutes.EditorScreen)
                        1 -> bottomNavController.navigate(SurfaceDestinationRoutes.AIScreen)
                        2 -> bottomNavController.navigate(SurfaceDestinationRoutes.CreationsScreen)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (index == selectedItem.intValue) {
                            item.selectedIcon
                        } else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                }
            )
        }
    }
}