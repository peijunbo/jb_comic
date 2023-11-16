package com.bedlier.jbcomic.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.SubcomposeAsyncImage
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.ImageViewModel
import com.bedlier.jbcomic.ui.navigation.LocalNavController
import com.bedlier.jbcomic.ui.theme.ElevationTokens
import com.bedlier.jbcomic.ui.theme.IconButtonStyle
import com.bedlier.jbcomic.ui.viewer.widgets.ConfigToggleItem
import com.bedlier.jbcomic.ui.viewer.widgets.ScalableLazyColumn
import com.elvishew.xlog.XLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ViewerScreen(
    imageViewModel: ImageViewModel
) {
    val singleMode = remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val viewIndex = remember { mutableIntStateOf(imageViewModel.viewIndex) }
    val scrollFlow = remember { MutableStateFlow(viewIndex.intValue) }

    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(3f)
                .fillMaxWidth(),
            visible = showMenu,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            ViewerAppBar()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
                .windowInsetsPadding(WindowInsets.safeContent)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        showMenu = !showMenu
                    },
                    onLongClick = {}
                )
        ) {
            ViewerPager(
                imageViewModel = imageViewModel,
                singleMode = singleMode.value,
                scrollFlow = scrollFlow,
                onIndexChange = {
                    viewIndex.intValue = it
                }
            )
        }
        AnimatedVisibility(
            visible = showMenu,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomCenter)
                .zIndex(2f),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            ViewerBottomSheet(
                singleMode = singleMode,
                viewIndex = viewIndex,
                maxIndex = imageViewModel.viewQueue.lastIndex,
                onModeClick = { singleMode.value = it },
                onIndexChange = {
                    coroutineScope.launch {
                        scrollFlow.emit(it)
                        XLog.d("ViewerScreen: emit $it")
                    }
                }
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerAppBar(
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    CenterAlignedTopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = {
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.button_back)
                )
            }
        },
        title = { /*TODO*/ }
    )
}

@Composable
fun ViewerPager(
    imageViewModel: ImageViewModel,
    singleMode: Boolean,
    scrollFlow: StateFlow<Int>,
    onIndexChange: (index: Int) -> Unit
) {
    if (singleMode) {
        HorizontalComicPager(imageViewModel = imageViewModel)
    } else {
        VerticalComicList(
            imageViewModel = imageViewModel,
            scrollFlow = scrollFlow,
            onIndexChange = onIndexChange
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalComicPager(
    imageViewModel: ImageViewModel
) {
    val state = rememberPagerState(
        initialPage = imageViewModel.viewIndex
    ) {
        imageViewModel.viewQueue.size
    }
    val images = imageViewModel.viewQueue
    HorizontalPager(state = state, key = {images[it].id}) { index ->
        val image = images[index]
        SubcomposeAsyncImage(
            model = image.uri,
            contentDescription = image.name,
            loading = {
                CircularProgressIndicator()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun VerticalComicList(
    imageViewModel: ImageViewModel,
    scrollFlow: StateFlow<Int>,
    onIndexChange: (index: Int) -> Unit
) {
    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = imageViewModel.viewIndex)
    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { index ->
                onIndexChange(index)
            }
    }
    LaunchedEffect(key1 = scrollFlow) {
        scrollFlow.collectLatest {
            XLog.d("VerticalComicList: collect and scroll to $it")
            if (it == lazyListState.firstVisibleItemIndex) return@collectLatest
            lazyListState.scrollToItem(it)
        }
    }
    val images = imageViewModel.viewQueue
    ScalableLazyColumn(
        lazyListState = lazyListState
    ) {
        scalableItems(images.size, key = {images[it].id}) { index: Int ->
            val image = images[index]
            SubcomposeAsyncImage(
                model = image.uri,
                contentDescription = image.name,
                loading = {
                    CircularProgressIndicator()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerBottomSheet(
    singleMode: State<Boolean>,
    viewIndex: State<Int>,
    maxIndex: Int,
    onModeClick: (singleMode: Boolean) -> Unit = {},
    onIndexChange: (index: Int) -> Unit = {}
) {
    val index by viewIndex
    Surface(
        modifier = Modifier
            .zIndex(2f),
        tonalElevation = ElevationTokens.Level1,
        shape = MaterialTheme.shapes.extraLarge.copy(
            bottomEnd = CornerSize(0.dp),
            bottomStart = CornerSize(0.dp)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
            Slider(
                value = index.coerceAtLeast(0).toFloat(),
                onValueChange = { onIndexChange(it.toInt()) },
                valueRange = 0f..maxIndex.coerceAtLeast(0).toFloat(),
            )
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

}