package com.bedlier.jbcomic

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController

class MainViewModel : ViewModel() {
    private lateinit var _navController: NavHostController
    val navController: NavController
        get() = _navController

    fun setUpNavController(navController: NavHostController) {
        _navController = navController
    }
}