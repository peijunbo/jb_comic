package com.bedlier.jbcomic.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.home.pages.AlbumPage
import com.bedlier.jbcomic.ui.home.pages.QueuePage
import com.bedlier.jbcomic.ui.home.pages.StoragePage
import com.bedlier.jbcomic.ui.theme.ElevationTokens
import kotlinx.coroutines.launch

private const val TAG = "HomeScreen"

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    // false stand for group by album, true stand for group by date
    val groupByDate by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
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
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false}
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(
                                        id = R.string.menu_photos_group_by, stringResource(
                                            id = R.string.menu_photos_group_by_date
                                        )
                                    )
                                )
                            },
                            trailingIcon = {Icons.Default.Sort},
                            onClick = { /*TODO*/ }
                        )
                    }
                }
            )
        },
        bottomBar = {
            HomeBottomBar(pagerState = pagerState, onSelect = {
                if (pagerState.currentPage != it) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(it)
                    }
                }
            })
        },
        content = {
            Surface(modifier = Modifier.padding(it)) {
                HomeContent(pagerState)
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    pagerState: PagerState
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page: Int ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                Page.Queue.ordinal -> {
                    QueuePage()
                }

                Page.Photos.ordinal -> {
                    AlbumPage()
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

/**
 * Group by bucket or date dialog
 * @param groupBy group by bucket or date, false stand for group by album, true stand for group by date
 * @param onDismissRequest callback when dialog dismiss
 */
@Composable
fun GroupDialog(
    groupBy: MutableState<Boolean>,
    onDismissRequest: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(text = stringResource(id = R.string.menu_photos_group_by), fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                Row {
                    Text(text = stringResource(id = R.string.menu_photos_group_by_album))
                    RadioButton(selected = groupBy.value, onClick = { groupBy.value = true })
                    Text(text = stringResource(id = R.string.menu_photos_group_by_date))
                    RadioButton(selected = !groupBy.value, onClick = { groupBy.value = false })
                }
                Row {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = stringResource(id = R.string.button_confirm))
                    }
                }
            }
        }
    }
}