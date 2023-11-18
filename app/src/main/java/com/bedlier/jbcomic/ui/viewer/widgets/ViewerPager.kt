package com.bedlier.jbcomic.ui.viewer.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import coil.compose.SubcomposeAsyncImage
import com.bedlier.jbcomic.ui.ImageViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
fun ViewerPager(
    imageViewModel: ImageViewModel,
    singleMode: Boolean,
    scrollFlow: StateFlow<Int>,
    onIndexChange: (index: Int) -> Unit,
    onOpenMenu: () -> Unit = {}
) {
    if (singleMode) {
        HorizontalComicPager(
            imageViewModel = imageViewModel,
            scrollFlow = scrollFlow,
            onIndexChange = onIndexChange,
            onOpenMenu = onOpenMenu
        )
    } else {
        VerticalComicList(
            imageViewModel = imageViewModel,
            scrollFlow = scrollFlow,
            onIndexChange = onIndexChange,
            onOpenMenu = onOpenMenu
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalComicPager(
    imageViewModel: ImageViewModel,
    scrollFlow: StateFlow<Int>,
    onIndexChange: (index: Int) -> Unit,
    onOpenMenu: () -> Unit = {}
) {
    val viewIndex = scrollFlow.collectAsState()
    val state = rememberPagerState(
        initialPage = viewIndex.value
    ) {
        imageViewModel.viewQueue.size
    }
    LaunchedEffect(Unit) {
        scrollFlow.collectLatest {
            if (it == state.currentPage) return@collectLatest
            state.animateScrollToPage(it)
        }
    }
    LaunchedEffect(key1 = state) {
        snapshotFlow { state.currentPage }.collectLatest {
            onIndexChange(it)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val images = imageViewModel.viewQueue
    HorizontalPager(
        state = state,
        userScrollEnabled = false,
        key = { images[it].id },
        modifier = Modifier
            .fillMaxSize()
            .horizontalPartitionTap(
                onLeftTap = {
                    if (state.currentPage == 0) return@horizontalPartitionTap
                    coroutineScope.launch {
                        state.animateScrollToPage(state.currentPage - 1)
                    }
                },
                onRightTap = {
                    if (state.currentPage == images.lastIndex) return@horizontalPartitionTap
                    coroutineScope.launch {
                        state.animateScrollToPage(state.currentPage + 1)
                    }
                },
                onMiddleTap = {
                    onOpenMenu()
                }
            )
    ) { index ->
        val image = images[index]
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = image.uri, contentDescription = image.name, loading = {
                    CircularProgressIndicator()
                }, modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun VerticalComicList(
    imageViewModel: ImageViewModel,
    scrollFlow: StateFlow<Int>,
    onIndexChange: (index: Int) -> Unit,
    onOpenMenu: () -> Unit = {}
) {
    val viewIndex = scrollFlow.collectAsState()
    val lazyListState =
        rememberLazyListState(initialFirstVisibleItemIndex = viewIndex.value)
    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }.collect { index ->
            onIndexChange(index)
        }
    }
    LaunchedEffect(key1 = scrollFlow) {
        scrollFlow.collectLatest {
            if (it == lazyListState.firstVisibleItemIndex) return@collectLatest
            lazyListState.animateScrollToItem(it)
        }
    }
    val images = imageViewModel.viewQueue
    ScalableLazyColumn(
        lazyListState = lazyListState
    ) {
        scalableItems(images.size, key = { images[it].id }) { index: Int ->
            val image = images[index]
            SubcomposeAsyncImage(
                model = image.uri, contentDescription = image.name, loading = {
                    CircularProgressIndicator()
                }, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

