package com.bedlier.jbcomic

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.bedlier.jbcomic.ui.navigation.LocalNavController
import com.bedlier.jbcomic.ui.navigation.NavContainer
import com.bedlier.jbcomic.ui.theme.JBComicTheme
import com.elvishew.xlog.XLog

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            CompositionLocalProvider(
                LocalNavController provides rememberNavController()
            ) {
                JBComicTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavContainer()
                    }
                }
            }
        }
    }
}
