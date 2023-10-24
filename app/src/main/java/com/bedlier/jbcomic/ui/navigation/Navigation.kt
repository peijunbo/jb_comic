package com.bedlier.jbcomic.ui.navigation

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.home.HomeScreen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

val destinations = listOf(
    Screen.Home.route,
    Screen.Settings.route,
    Screen.About.route
)
@Composable
fun NavContainer() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                destinations.forEach {
                    NavigationDrawerItem(label = {
                        Text(text = it)
                    }, icon = {
                        Icon(
                            imageVector = when (it) {
                                stringResource(id = R.string.nav_destination_home) -> Icons.Default.Home
                                stringResource(id = R.string.nav_destination_settings) -> Icons.Default.Settings
                                stringResource(id = R.string.nav_destination_about) -> Icons.Default.Error
                                else -> Icons.Default.ArrowRight
                            },
                            contentDescription = it
                        )
                    }, selected = false, onClick = {navController.navigate(it)})
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = destinations[0]) {
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
        }
    }

}
