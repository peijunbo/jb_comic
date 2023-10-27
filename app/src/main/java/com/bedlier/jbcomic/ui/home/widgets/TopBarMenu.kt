package com.bedlier.jbcomic.ui.home.widgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.bedlier.jbcomic.R

@Composable
fun AlbumPageMenu(
    onOpenDialog: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
    }
    DropdownMenu(
        expanded = expanded, onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(
                        id =  R.string.menu_album_sort
                    )
                )
            },
            trailingIcon = { Icons.Default.Sort },
            onClick = onOpenDialog
        )
    }
}