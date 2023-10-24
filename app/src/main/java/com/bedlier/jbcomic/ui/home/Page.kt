package com.bedlier.jbcomic.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ViewList

import androidx.compose.ui.graphics.vector.ImageVector

enum class Page(val title: String, val icon: ImageVector) {
    Photos("Photos", Icons.Default.PhotoLibrary),
    Storage("Storage", Icons.Default.Folder),
    Queue("Queue", Icons.Default.ViewList)
}
