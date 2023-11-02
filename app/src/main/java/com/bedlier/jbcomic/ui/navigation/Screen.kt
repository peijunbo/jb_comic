package com.bedlier.jbcomic.ui.navigation

import androidx.annotation.StringRes
import com.bedlier.jbcomic.R

sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val showInDrawer: Boolean = true) {
    data object Home : Screen("home", R.string.nav_destination_home)
    data object Settings : Screen("settings", R.string.nav_destination_settings)
    data object About : Screen("about", R.string.nav_destination_about)
    data object Viewer : Screen("viewer", R.string.nav_destination_viewer, false)
}
