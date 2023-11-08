package com.bedlier.jbcomic.ui.viewer

import android.util.Log
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.ImageViewModel
import com.bedlier.jbcomic.ui.navigation.LocalNavController
import com.bedlier.jbcomic.ui.navigation.Screen
import com.bedlier.jbcomic.ui.theme.IconButtonStyle
import com.bedlier.jbcomic.ui.viewer.widgets.ConfigToggleItem
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

private const val TAG = "ViewerScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ViewerScreen(
    imageViewModel: ImageViewModel = viewModel()
) {
    val singleMode = remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            /*.clickable(
                interactionSource = interactionSource,
                indication = null
            ) { showBottomSheet = true }*/
            .windowInsetsPadding(WindowInsets.safeContent)
    ) {
        ViewerPager(singleMode = singleMode.value)
        if (showBottomSheet) {
            ViewerBottomSheet(
                singleMode = singleMode,
                sheetState = bottomSheetState,
                onDismissRequest = {
                    showBottomSheet = false
                },
                onModeClick = { singleMode.value = it }
            )
        }
    }

}


@Composable
fun ViewerPager(
    singleMode: Boolean
) {
    if (singleMode) {
        Text(text = "Single")
    } else {
        VerticalComicList()
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VerticalComicList(
    imageViewModel: ImageViewModel = viewModel(LocalNavController.current.getBackStackEntry(Screen.Home.route))
) {
    var scale = remember { mutableFloatStateOf(1f) }
    var offset: MutableState<Offset> = remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale.floatValue *= zoomChange
        offset.value += panChange
        Log.d(TAG, "VerticalComicList: scale $scale")
        Log.d(TAG, "VerticalComicList: offset $offset")
    }

    ScalableLazyColumn(
//        scale = scale,
//        offset = offset
    ) {
        scalableItems(imageViewModel.viewQueue.size) {index: Int ->
            val image = imageViewModel.viewQueue[index]
            GlideImage(
                model = image.uri,
                contentDescription = image.name,
                loading = placeholder(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .fillMaxSize()
//                    .graphicsLayer {
//                        scaleX = scale.floatValue
//                        scaleY = scale.floatValue
//                        translationX = offset.value.x
//                    }
            )
        }
    }
//    LazyColumn(
//        userScrollEnabled = false,
//        state = listState,
//        contentPadding = PaddingValues(all = 16.dp),
//        modifier = Modifier
//            .scalableScroll(listState, Orientation.Vertical, transformableState),
//    ) {
//        items(imageViewModel.viewQueue.size) { index ->
//            val image = imageViewModel.viewQueue[index]
//            Layout(
//                modifier = Modifier
//                    .background(randomColor())
//                    .padding(16.dp),
//                content = {
//                    GlideImage(
//                        model = image.uri,
//                        contentDescription = image.name,
//                        loading = placeholder(R.drawable.ic_launcher_foreground),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .graphicsLayer {
//                                scaleX = scale
//                                scaleY = scale
//                                translationX = offsetX
//
//                            }
//                    )
//                },
//            ) { measurables: List<Measurable>, constraints: Constraints ->
//                val placeables = measurables.map { measurable ->
//                    measurable.measure(constraints)
//                }
//                XLog.d("VerticalComicList: placeables size ${placeables[0].measuredHeight}")
//                layout(placeables[0].measuredWidth, (placeables[0].measuredHeight * scale).roundToInt()) {
//                    XLog.d("VerticalComicList: layout ${placeables[0].measuredWidth} ${placeables[0].measuredHeight}")
//                    XLog.d("VerticalComicList: actual size ${this.coordinates?.size}")
//                    val offsetY = (placeables[0].measuredHeight * scale).roundToInt() - placeables[0].measuredHeight
//                    placeables.forEachIndexed { _, placeable ->
//                        placeable.placeRelative(0, offsetY / 2)
//                    }
//                }
//            }
//        }
//    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ViewerBottomSheet(
    singleMode: State<Boolean>,
    sheetState: SheetState,
    onDismissRequest: () -> Unit = {},
    onModeClick: (singleMode: Boolean) -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(IconButtonStyle.ExtraLarge),
            userScrollEnabled = false,
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                ConfigToggleItem(
                    icon = Icons.Outlined.LooksOne,
                    checked = singleMode.value,
                    text = "Single",
                    onCheckedChange = onModeClick
                )
            }
        }
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

fun randomColor(): Color {
    return Color(
        red = (0..255).random(),
        green = (0..255).random(),
        blue = (0..255).random()
    )
}