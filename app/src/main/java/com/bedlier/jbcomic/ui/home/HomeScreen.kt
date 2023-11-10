package com.bedlier.jbcomic.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.ImageViewModel
import com.bedlier.jbcomic.ui.home.pages.AlbumPage
import com.bedlier.jbcomic.ui.home.pages.PhotoPage
import com.bedlier.jbcomic.ui.home.pages.QueuePage
import com.bedlier.jbcomic.ui.home.pages.StoragePage
import com.bedlier.jbcomic.ui.home.widgets.AlbumDialog
import com.bedlier.jbcomic.ui.home.widgets.AlbumPageMenu
import com.bedlier.jbcomic.ui.home.widgets.PhotoPageMenu
import com.bedlier.jbcomic.ui.theme.ElevationTokens
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TAG = "HomeScreen"

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit = {},
    imageViewModel: ImageViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { Page.entries.size })
    var showAlbumDialog by remember { mutableStateOf(false) }
    if (showAlbumDialog) {
        AlbumDialog(currentState = imageViewModel.albumSortState.value, onDismissRequest = {
            showAlbumDialog = false
        }, onConfirm = { state ->
            imageViewModel.albumSortState.value = state
            showAlbumDialog = false
        })
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                modifier = Modifier.shadow(elevation = ElevationTokens.Level2),
                title = {
                    Text(text = "JBComic")
                },
                navigationIcon = {
                    IconButton(onClick = { onOpenDrawer() }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                        )
                    }
                },
                actions = {
                    when (pagerState.currentPage) {
                        Page.Album.ordinal -> AlbumPageMenu(
                            onOpenDialog = {
                                showAlbumDialog = true
                            },
                            onRefresh = {
                                imageViewModel.loadImageStore()
                            }
                        )

                        Page.Photo.ordinal -> PhotoPageMenu(
                            onRefresh = {
                                imageViewModel.loadImageStore()
                            }
                        )

                        else -> {}
                    }
                }
            )
        },
        bottomBar = {
            HomeBottomBar(pagerState = pagerState, onSelect = {
                if (pagerState.currentPage != it) {
                    coroutineScope.launch {
                        if (abs(pagerState.currentPage - it) == 1) {
                            pagerState.animateScrollToPage(it)
                        } else {
                            pagerState.scrollToPage(it)
                        }
                    }
                }
            })
        },
        content = {
            Surface(modifier = Modifier.padding(it)) {
                HomeContent(imageViewModel = imageViewModel, pagerState = pagerState)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    imageViewModel: ImageViewModel,
    pagerState: PagerState
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondBoundsPageCount = 3
    ) { page: Int ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                Page.Photo.ordinal -> {
                    PhotoPage(imageViewModel = imageViewModel)
                }

                Page.Queue.ordinal -> {
                    QueuePage()
                }

                Page.Album.ordinal -> {
                    AlbumPage(imageViewModel = imageViewModel)
                }

                Page.Storage.ordinal -> {
                    StoragePage()
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBottomBar(
    pagerState: PagerState,
    onSelect: (Int) -> Unit
) {
    NavigationBar {
        Page.entries.forEachIndexed { index, page ->
            NavigationBarItem(
                icon = { Icon(imageVector = page.icon, contentDescription = page.title) },
                label = { Text(text = page.title) },
                selected = index == pagerState.currentPage,
                onClick = { onSelect(index) }
            )
        }
    }
}
