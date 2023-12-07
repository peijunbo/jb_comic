package com.bedlier.jbcomic.ui.home.pages

import android.app.Activity
import android.service.autofill.OnClickAction
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.StorageViewModel
import java.io.File

@Composable
fun StoragePage(
    storageViewModel: StorageViewModel = viewModel()
) {
    var permissionGranted = remember { mutableStateOf(storageViewModel.checkPermission()) }
    if (permissionGranted.value) {
        StorageFileList()
    } else {
        val activity = LocalContext.current as Activity
        Text(text = stringResource(id = R.string.message_storage_permission))
        Button(onClick = {
            storageViewModel.requestPermission(activity) { _: MutableList<String>, allGranted: Boolean ->
                if (allGranted) {
                    permissionGranted.value = true
                }
            }
        }) {
            Text(text = stringResource(id = R.string.action_request_permission))
        }
    }
}

@Composable
fun StorageFileList(
    storageViewModel: StorageViewModel = viewModel()
) {
    val currentFiles = storageViewModel.currentFiles
    val currentDir = storageViewModel.currentDir
    if (currentFiles.isEmpty()) {
        Text(
            text = stringResource(id = R.string.message_no_file),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(currentFiles) { file ->
                StorageFileListItem(file = file, onClick = {
                    storageViewModel.clickFile(file)
                })
            }
        }
    }
    BackHandler(
        enabled = currentDir.value != storageViewModel.rootDir,
    ) {
        storageViewModel.clickFile(currentDir.value.parentFile ?: storageViewModel.rootDir)
    }
}

@Composable
fun StorageFileListItem(
    file: File,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageVector =
            if (file.isDirectory) {
                Icons.Default.Folder
            } else {
                // judge its extended name
                val extendedName = file.name.substringAfterLast('.')
                when (extendedName) {
                    // image
                    "jpg", "png", "jpeg", "webp", "bmp", "gif" -> {
                        Icons.Default.Image
                    }

                    "zip", "rar", "7z", "tar", "gz", "bz2" -> {
                        Icons.Default.FolderZip
                    }

                    "docx", "doc" -> {
                        Icons.Default.Description
                    }

                    "xlsx", "xls", "csv" -> {
                        Icons.Default.TableChart
                    }

                    else -> {
                        Icons.Default.Dataset
                    }
                }
            }
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary)
        Text(
            text = file.name,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
