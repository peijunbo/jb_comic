package com.bedlier.jbcomic.ui.viewer

import android.util.Range
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.panBy
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.rotateBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.zoomBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.elvishew.xlog.XLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.scalableScroll(
    state: ScrollableState,
    orientation: Orientation,
    transformableState: TransformableState,
    enabled: Boolean = true,
    rotationEnabled: Boolean = false,
) = this.composed {
    val coroutineScope = rememberCoroutineScope()
    val flingBehavior = ScrollableDefaults.flingBehavior()
    val overscrollEffect = ScrollableDefaults.overscrollEffect()
    val channel = remember { Channel<ScaleScrollEvent>(Channel.UNLIMITED) }
    if (enabled) {
        LaunchedEffect(state) {
            var flingJob: Job? = null
            while (isActive) {
                val event = channel.receive()
                try {
                    if (flingJob != null && flingJob.isActive) {
                        flingJob.cancel()
                    }
                    when (event) {
                        is ScaleScrollEvent.ScaleDelta -> {
                            if (event.zoomChange != 1f) {
                                transformableState.zoomBy(event.zoomChange)
                            }
                            if (event.panChange != Offset.Zero) {
                                transformableState.panBy(event.panChange)
                            }
                            if (event.rotationChange != 0f) {
                                transformableState.rotateBy(event.rotationChange)
                            }
                        }

                        is ScaleScrollEvent.ScrollDelta -> {
                            if (orientation == Orientation.Vertical) {
                                val rest = state.scrollBy(-event.delta.y)
                                overscrollEffect.applyToScroll(
                                    Offset(0f, event.delta.y),
                                    NestedScrollSource.Drag,
                                ) {
                                    Offset(0f, -rest)
                                }
                            } else {
                                val rest = state.scrollBy(-event.delta.x)
                                overscrollEffect.applyToScroll(
                                    Offset(event.delta.x, 0f),
                                    NestedScrollSource.Drag,
                                ) {
                                    Offset(-rest, 0f)
                                }
                            }
                        }

                        is ScaleScrollEvent.ScaleScrollStopped -> {
                            state.scroll {
                                with(flingBehavior) {
                                    flingJob = coroutineScope.launch {
                                        overscrollEffect.applyToFling(
                                            if (orientation == Orientation.Vertical) event.velocity.copy(
                                                x = 0f
                                            )
                                            else event.velocity.copy(y = 0f),
                                            performFling = {
                                                if (orientation == Orientation.Vertical) {
                                                    Velocity(0f, performFling(-it.y)) - it
                                                } else {
                                                    Velocity(performFling(-it.x), 0f) - it
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        else -> {}
                    }

                } catch (_: CancellationException) {
                    // ignore the cancellation and start over again.
                }
            }
        }
    }
    val block: suspend PointerInputScope.() -> Unit = remember {
        {
            val velocityTracker = VelocityTracker()
            coroutineScope {
                awaitEachGesture {
                    try {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (!canceled) {
                                val positionChanges = event.changes.filter { it.positionChanged() }
                                if (positionChanges.size > 1) {
                                    val pan = event.calculatePan()
                                    val zoom = event.calculateZoom()
                                    val rotation = event.calculateRotation()
                                    channel.trySend(
                                        ScaleScrollEvent.ScaleDelta(
                                            zoomChange = zoom,
                                            panChange = pan,
                                            rotationChange = if (rotationEnabled) rotation else 0f
                                        )
                                    )
                                    positionChanges.forEach {
                                        it.consume()
                                    }
                                } else if (positionChanges.size == 1) {
                                    val pan = event.calculatePan()
                                    channel.trySend(ScaleScrollEvent.ScaleDelta(1f, pan, 0f))
                                    velocityTracker.addPointerInputChange(event.changes.first())
                                    if (pan != Offset.Zero) {
                                        channel.trySend(ScaleScrollEvent.ScrollDelta(pan))
                                    }
                                }
                            }
                        } while (!canceled && event.changes.any { it.pressed })
                    } catch (e: CancellationException) {
                        if (!isActive) throw e
                    } finally {
                        channel.trySend(
                            ScaleScrollEvent.ScaleScrollStopped(
                                velocityTracker.calculateVelocity()
                            )
                        )
                        velocityTracker.resetTracking()
                    }
                }
            }
        }
    }
    return@composed Modifier
        .pointerInput(channel, block = block)
        .then(overscrollEffect.effectModifier)
    //.scale(scale)
}

private sealed class ScaleScrollEvent {
    data object ScaleScrollStarted : ScaleScrollEvent()
    data class ScaleScrollStopped(
        val velocity: Velocity
    ) : ScaleScrollEvent()

    data class ScrollDelta(
        val delta: Offset
    ) : ScaleScrollEvent()

    data class ScaleDelta(
        val zoomChange: Float,
        val panChange: Offset,
        val rotationChange: Float
    ) : ScaleScrollEvent()
}

interface ScalableScrollScope : LazyListScope {
    abstract fun scalableItem(
        key: Any? = null,
        contentType: Any? = null,
        content: @Composable LazyItemScope.() -> Unit
    )

    abstract fun scalableItems(
        count: Int,
        key: ((index: Int) -> Any)? = null,
        contentType: (index: Int) -> Any? = { null },
        itemContent: @Composable LazyItemScope.(index: Int) -> Unit
    )
}

@Composable
fun ScalableLayout(
    scale: Float,
    offset: Offset = Offset.Zero,
    orientation: Orientation,
    onSizeChanged: (size: IntSize) -> Unit = {},
    content: @Composable () -> Unit,
) {
    Layout(
        content = {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = if (orientation == Orientation.Vertical) offset.x else 0f,
                        translationY = if (orientation == Orientation.Horizontal) offset.y else 0f
                    )
            ) {
                content()
            }
        },
        modifier = Modifier.onSizeChanged(onSizeChanged = onSizeChanged)
    ) { measurables: List<Measurable>, constraints: Constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        val maxWidth = placeables.maxOfOrNull { it.width } ?: 0
        val maxHeight = placeables.maxOfOrNull { it.height } ?: 0
        val layoutWidth =
            if (orientation == Orientation.Horizontal) (maxWidth * scale).roundToInt() else maxWidth
        val layoutHeight =
            if (orientation == Orientation.Vertical) (maxHeight * scale).roundToInt() else maxHeight
        layout(
            layoutWidth,
            layoutHeight
        ) {
            if (orientation == Orientation.Vertical) {
                val offsetY =
                    (layoutHeight * scale).roundToInt() - layoutHeight
                placeables.forEachIndexed { _, placeable ->
                    placeable.placeRelative(0, offsetY / 2)
                }
            } else {
                val offsetX =
                    (layoutWidth * scale).roundToInt() - layoutWidth
                placeables.forEachIndexed { _, placeable ->
                    placeable.placeRelative(offsetX / 2, 0)
                }
            }
        }
    }
}

private class ScalableScrollScopeImpl(
    lazyListScope: LazyListScope,
    val scale: MutableFloatState,
    val offset: MutableState<Offset>,
    val orientation: Orientation
) : ScalableScrollScope, LazyListScope by lazyListScope {
    override fun scalableItem(
        key: Any?,
        contentType: Any?,
        content: @Composable() (LazyItemScope.() -> Unit)
    ) {
        item(
            key = key,
            contentType = contentType
        ) {
            ScalableLayout(
                scale = this@ScalableScrollScopeImpl.scale.floatValue,
                offset = this@ScalableScrollScopeImpl.offset.value,
                orientation = this@ScalableScrollScopeImpl.orientation
            ) {
                content()
            }
        }
    }

    override fun scalableItems(
        count: Int,
        key: ((index: Int) -> Any)?,
        contentType: (index: Int) -> Any?,
        itemContent: @Composable() (LazyItemScope.(index: Int) -> Unit)
    ) {
        items(
            count = count,
            key = key,
            contentType = contentType,
        ) { index: Int ->
            ScalableLayout(
                scale = this@ScalableScrollScopeImpl.scale.floatValue,
                offset = this@ScalableScrollScopeImpl.offset.value,
                orientation = this@ScalableScrollScopeImpl.orientation
            ) {
                itemContent(index)
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScalableLazyColumn(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    transformableState: TransformableState? = null,
    scaleRange: Range<Float> = Range(1f, 5f),
    content: ScalableScrollScope.() -> Unit
) {
    val scale: MutableFloatState = remember { mutableFloatStateOf(1f) }
    val offset: MutableState<Offset> = remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val mTransformableState =
        transformableState ?: rememberTransformableState { zoomChange, panChange, _ ->
            scale.floatValue *= zoomChange
            if (scale.floatValue < scaleRange.lower) {
                scale.floatValue = scaleRange.lower
            } else if (scale.floatValue > scaleRange.upper) {
                scale.floatValue = scaleRange.upper
            }
            offset.value += panChange
            if (offset.value.x > size.width * (scale.floatValue - 1f) / 2f) {
                offset.value = Offset(size.width * (scale.floatValue - 1f) / 2f, offset.value.y)
            } else if (offset.value.x < -size.width * (scale.floatValue - 1f) / 2f) {
                offset.value = Offset(-size.width * (scale.floatValue - 1f) / 2f, offset.value.y)
            }
            XLog.d("ScalableLazyColumn: offset ${offset.value}")
        }
    LazyColumn(
        userScrollEnabled = false,
        state = lazyListState,
        contentPadding = PaddingValues(all = 16.dp),
        modifier = modifier
            .scalableScroll(lazyListState, Orientation.Vertical, mTransformableState)
            .onSizeChanged { newSize: IntSize ->
                size = newSize
            },
    ) {
        val scalableScrollScope = ScalableScrollScopeImpl(this, scale, offset, Orientation.Vertical)
        with(scalableScrollScope) {
            content()
        }
    }
}

@Composable
fun ScalableLazyRow(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    transformableState: TransformableState? = null,
    scaleRange: Range<Float> = Range(1f, 5f),
    content: ScalableScrollScope.() -> Unit
) {
    val scale: MutableFloatState = remember { mutableFloatStateOf(1f) }
    val offset: MutableState<Offset> = remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val mTransformableState =
        transformableState ?: rememberTransformableState { zoomChange, panChange, _ ->
            scale.floatValue *= zoomChange
            if (scale.floatValue < scaleRange.lower) {
                scale.floatValue = scaleRange.lower
            } else if (scale.floatValue > scaleRange.upper) {
                scale.floatValue = scaleRange.upper
            }
            offset.value += panChange
            if (offset.value.y > size.height * (scale.floatValue - 1f) / 2f) {
                offset.value = Offset(offset.value.x, size.height * (scale.floatValue - 1f) / 2f)
            } else if (offset.value.y < -size.height * (scale.floatValue - 1f) / 2f) {
                offset.value = Offset(offset.value.x, -size.height * (scale.floatValue - 1f) / 2f)
            }
            XLog.d("ScalableLazyRow: offset ${offset.value}")
        }
    LazyRow(
        userScrollEnabled = false,
        state = lazyListState,
        contentPadding = PaddingValues(all = 16.dp),
        modifier = modifier
            .scalableScroll(lazyListState, Orientation.Horizontal, mTransformableState)
            .onSizeChanged { newSize: IntSize ->
                size = newSize
            },
    ) {
        val scalableScrollScope =
            ScalableScrollScopeImpl(this, scale, offset, Orientation.Horizontal)
        with(scalableScrollScope) {
            content()
        }
    }
}

