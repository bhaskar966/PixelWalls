package com.bhaskar.pixelwalls

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.bhaskar.pixelwalls.presentation.navigation.AppNavigation

//import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
//@Preview
fun App() {
//    initKoin()
    MaterialTheme {
//        var showContent by remember { mutableStateOf(false) }
//        Column(
//            modifier = Modifier
//                .background(MaterialTheme.colorScheme.primaryContainer)
//                .safeContentPadding()
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            Button(onClick = { showContent = !showContent }) {
//                Text("Click me!")
//            }
////            AnimatedVisibility(showContent) {
//////                val greeting = remember { Greeting().greet() }
////                Column(
////                    modifier = Modifier.fillMaxWidth(),
////                    horizontalAlignment = Alignment.CenterHorizontally,
////                ) {
////                    Image(painterResource(Res.drawable.compose_multiplatform), null)
////                    Text("Compose: $greeting")
////                }
////            }
//        }
//    }

        AppNavigation()
//        SimpleTestScreen()
    }
}