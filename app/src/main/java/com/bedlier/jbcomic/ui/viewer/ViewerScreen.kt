package com.bedlier.jbcomic.ui.viewer

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
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
import com.bumptech.glide.load.model.stream.HttpGlideUrlLoader
import com.elvishew.xlog.XLog
import kotlin.math.roundToInt

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
    val interactionSource = remember { MutableInteractionSource() }
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
                    Log.d(TAG, "ViewerScreen: onDismiss")
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun VerticalComicList(
    imageViewModel: ImageViewModel = viewModel(LocalNavController.current.getBackStackEntry(Screen.Home.route))
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale *= zoomChange
        offsetX += panChange.x
        Log.d(TAG, "VerticalComicList: scale $scale")
        Log.d(TAG, "VerticalComicList: offset $offsetX")
    }

    LazyColumn(
        userScrollEnabled = false,
        state = listState,
        modifier = Modifier
            .scalableScroll(listState, Orientation.Vertical, transformableState),
    ) {
        items(imageViewModel.viewQueue.size) { index ->
            val image = imageViewModel.viewQueue[index]
            Layout(
                modifier = Modifier.background(randomColor()),
                content = {
                    GlideImage(
                        model = image.uri,
                        contentDescription = image.name,
                        loading = placeholder(R.drawable.ic_launcher_foreground),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                            }
                    )
                },
            ) { measurables: List<Measurable>, constraints: Constraints ->
                val placeables = measurables.map { measurable ->
                    measurable.measure(constraints)
                }
                XLog.d("VerticalComicList: placeables size ${placeables[0].measuredHeight}")
                layout(placeables[0].measuredWidth, (placeables[0].measuredHeight * scale).roundToInt()) {
                    XLog.d("VerticalComicList: layout ${placeables[0].measuredWidth} ${placeables[0].measuredHeight}")
                    XLog.d("VerticalComicList: actual size ${this.coordinates?.size}")
                    var offsety = (placeables[0].measuredHeight * scale).roundToInt() - placeables[0].measuredHeight
                    placeables.forEachIndexed { index, placeable ->
                        placeable.placeRelative(0, offsety / 2)
                    }
                }
            }
        }
    }

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
            contentPadding = PaddingValues(16.dp)
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