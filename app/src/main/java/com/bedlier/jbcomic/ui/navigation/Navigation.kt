package com.bedlier.jbcomic.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.home.HomeScreen
import com.bedlier.jbcomic.ui.viewer.ViewerScreen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

val destinations = listOf(
    Screen.Home.route,
    Screen.Settings.route,
    Screen.About.route,
    Screen.Viewer.route
)
val LocalNavController = compositionLocalOf<NavHostController> {
    error("No Nav Controller")
}

@Composable
fun NavContainer() {
    val navController = LocalNavController.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                destinations.forEach { route: String ->
                    NavigationDrawerItem(label = {
                        Text(text = route)
                    }, icon = {
                        Icon(
                            imageVector = when (route) {
                                stringResource(id = R.string.nav_destination_home) -> Icons.Default.Home
                                stringResource(id = R.string.nav_destination_settings) -> Icons.Default.Settings
                                stringResource(id = R.string.nav_destination_about) -> Icons.Default.Error
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
            startDestination = destinations[0],
            enterTransition = {
                slideIn(
                    animationSpec = tween(500),
                    initialOffset = { IntOffset(0, it.height) }
                )
            },
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            composable(destinations[0]) {
                HomeScreen(onOpenDrawer = {
                    coroutineScope.launch { drawerState.open() }
                })
            }
            composable(destinations[1]) {

                Text(text = "Settings")
            }
            composable(destinations[2]) {
                Text(text = "About")
            }
            composable(destinations[3]) {
                ViewerScreen()
            }
        }
    }

}
