package com.bedlier.jbcomic.ui.home.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.home.ImageViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun PhotoPage(
    imageViewModel: ImageViewModel = viewModel()
) {
    var permissionGranted by remember { mutableStateOf(imageViewModel.checkPermission()) }
    if (permissionGranted) {
        LaunchedEffect(key1 = Unit) {
            imageViewModel.loadImageStore()
        }
        PhotoPageContent()
    } else {
        LaunchedEffect(key1 = Unit) {
            imageViewModel.requestPermission { _: MutableList<String>, allGranted: Boolean ->
                if (allGranted) {
                    imageViewModel.loadImageStore()
                    permissionGranted = true
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.message_no_permission))
            Button(
                onClick = {
                    imageViewModel.requestPermission { _: MutableList<String>, allGranted: Boolean ->
                        if (allGranted) {
                            imageViewModel.loadImageStore()
                            permissionGranted = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(id = R.string.action_request_permission),
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PhotoPageContent(
    imageViewModel: ImageViewModel = viewModel()
) {
    if (imageViewModel.imageList.isEmpty()) {
        Text(text = "No Image")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        imageViewModel.imagesGroupByDate.entries.forEach {(date, images) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = date)
            }
            items(images, key = { it.id }) { image ->
                GlideImage(
                    model = image.uri,
                    contentDescription = image.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(128.dp) // required for aspect ratio
                        .aspectRatio(1f)
                )
            }
        }
    }
}