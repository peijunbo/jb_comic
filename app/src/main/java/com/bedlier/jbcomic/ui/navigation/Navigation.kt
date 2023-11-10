package com.bedlier.jbcomic.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.ImageViewModel
import com.bedlier.jbcomic.ui.home.HomeScreen
import com.bedlier.jbcomic.ui.viewer.ViewerScreen
import com.elvishew.xlog.XLog
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
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = when (currentBackStackEntry?.destination?.route) {
            Screen.Viewer.route -> false
            else -> true
        },
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.618f),
            ) {
                Text(
                    text = stringResource(id = R.string.label_navigation),
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(top = 16.dp),
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                )
                Spacer(modifier = Modifier.height(8.dp))
                drawerDestinations.forEach { route: String ->
                    NavigationDrawerItem(
                        label = {
                            Text(text = route)
                        },
                        icon = {
                            Icon(
                                imageVector = when (route) {
                                    Screen.Home.route -> Icons.Default.Home
                                    Screen.Settings.route -> Icons.Default.Settings
                                    Screen.About.route -> Icons.Default.Error
                                    else -> Icons.Default.ArrowRight
                                },
                                contentDescription = route
                            )
                        },
                        selected = route == currentBackStackEntry?.destination?.route,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            // replace current route
                            navController.navigate(route) {
                                popUpTo(0) {
                                    saveState = true
                                }
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        val imageViewModel: ImageViewModel = viewModel()
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
                HomeScreen(
                    onOpenDrawer = {
                        coroutineScope.launch { drawerState.open() }
                    },
                    imageViewModel = imageViewModel
                )
            }
            composable(Screen.Settings.route) {
                Text(text = "Settings")
            }
            composable(Screen.About.route) {
                Text(text = "About")
            }
            composable(Screen.Viewer.route) {
                ViewerScreen(imageViewModel = imageViewModel)
            }
        }
    }

}