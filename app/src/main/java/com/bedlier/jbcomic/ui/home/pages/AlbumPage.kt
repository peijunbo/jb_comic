package com.bedlier.jbcomic.ui.home.pages

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.data.media.MediaImage
import com.bedlier.jbcomic.ui.ImageViewModel
import com.bedlier.jbcomic.ui.navigation.LocalNavController
import com.bedlier.jbcomic.ui.navigation.Screen
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun AlbumPage(
    imageViewModel: ImageViewModel = viewModel()
) {
    var permissionGranted by remember { mutableStateOf(imageViewModel.checkPermission()) }
    if (permissionGranted) {
        LaunchedEffect(key1 = Unit) {
            imageViewModel.requestImageStore()
        }
        AlbumPageContent()
    } else {
        val activity = LocalContext.current as Activity
        LaunchedEffect(key1 = Unit) {
            imageViewModel.requestPermission(activity = activity) { _: MutableList<String>, allGranted: Boolean ->
                if (allGranted) {
                    imageViewModel.requestImageStore()
                    permissionGranted = true
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.message_no_permission))
            val activity = LocalContext.current as Activity
            Button(
                onClick = {
                    imageViewModel.requestPermission(activity = activity) { _: MutableList<String>, allGranted: Boolean ->
                        if (allGranted) {
                            imageViewModel.requestImageStore()
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

@Composable
fun AlbumPageContent(
    imageViewModel: ImageViewModel = viewModel()
) {
    if (imageViewModel.albums.isEmpty()) {
        Text(text = "No Image")
        return
    }
    val navController = LocalNavController.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        imageViewModel.albums.entries.forEach { (bucketId, images) ->
            item(
                key = bucketId
            ) {

                AlbumItem(images = images) {
                    navController.navigate(Screen.Viewer.route)
                }
            }
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumItem(
    images: List<MediaImage>,
    onClick: () -> Unit = {}
) {
    Box(modifier = Modifier.clickable {
        onClick()
    }) {
        GlideImage(
            model = images[0].uri,
            contentDescription = images[0].name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.612f)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = images[0].bucketName,
            )
            Text(
                text = images.size.toString(),
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
    }
}