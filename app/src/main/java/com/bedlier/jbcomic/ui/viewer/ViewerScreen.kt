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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.LooksOne
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.ImageViewModel
import com.bedlier.jbcomic.ui.navigation.LocalNavController
import com.bedlier.jbcomic.ui.navigation.Screen
import com.bedlier.jbcomic.ui.theme.ElevationTokens
import com.bedlier.jbcomic.ui.theme.IconButtonStyle
import com.bedlier.jbcomic.ui.viewer.widgets.ConfigToggleItem
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun ViewerScreen(
    imageViewModel: ImageViewModel = viewModel()
) {
    val singleMode = remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
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
                )
        ) {
            ViewerPager(singleMode = singleMode.value)
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
                onModeClick = { singleMode.value = it }
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
    ScalableLazyColumn {
        scalableItems(imageViewModel.viewQueue.size) { index: Int ->
            val image = imageViewModel.viewQueue[index]
            GlideImage(
                model = image.uri,
                contentDescription = image.name,
                loading = placeholder(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerBottomSheet(
    singleMode: State<Boolean>,
    onModeClick: (singleMode: Boolean) -> Unit = {},
) {
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