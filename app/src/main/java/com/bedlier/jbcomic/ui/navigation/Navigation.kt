package com.bedlier.jbcomic.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bedlier.jbcomic.ui.home.HomeScreen
import com.bedlier.jbcomic.ui.viewer.ViewerScreen
import kotlinx.coroutines.launch

val drawerDestinations = listOf(
    Screen.Home.route,
    Screen.Settings.route,
    Screen.About.route
)
val LocalNavController = compositionLocalOf<NavHostController> {
    error("No Nav Controller")
}

@Composable
fun NavContainer() {
    val navController = LocalNavController.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var enableDrawerGestures by remember{ mutableStateOf(false) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enableDrawerGestures,
        drawerContent = {
            ModalDrawerSheet {
                drawerDestinations.forEach { route: String ->
                    NavigationDrawerItem(label = {
                        Text(text = route)
                    }, icon = {
                        Icon(
                            imageVector = when (route) {
                                Screen.Home.route -> Icons.Default.Home
                                Screen.Settings.route -> Icons.Default.Settings
                                Screen.About.route -> Icons.Default.Error
                                else -> Icons.Default.ArrowRight
                            },
                            contentDescription = route
                        )
                    }, selected = false, onClick = {
                        coroutineScope.launch { drawerState.close() }
                        // replace current route
                        navController.navigate(route) {
                            popUpTo(0) {
                                saveState = true
                            }
                            restoreState = true
                        }
                    })
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            enterTransition = {
                slideIn(
                    animationSpec = tween(500),
                    initialOffset = { IntOffset(0, it.height) }
                )
            },
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onOpenDrawer = {
                    coroutineScope.launch { drawerState.open() }
                })
            }
            composable(Screen.Settings.route) {
                Text(text = "Settings")
            }
            composable(Screen.About.route) {
                Text(text = "About")
            }
            composable(Screen.Viewer.route) {
                ViewerScreen()
            }
        }
    }
    
}