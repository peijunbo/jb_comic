package com.bedlier.jbcomic.ui.viewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animatePanBy
import androidx.compose.foundation.gestures.animateRotateBy
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.animateZoomBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.panBy
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.rotateBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.zoomBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.Velocity
import com.elvishew.xlog.XLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.scalableScroll(
    state: ScrollableState,
    orientation: Orientation,
    enabled: Boolean = true,
    rotationEnabled: Boolean = false,
) = this.composed {
    var scale by remember { mutableFloatStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()
    var offset by remember {
        mutableStateOf(Offset.Zero)
    }
    val transformableState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        scale *= zoomChange
        XLog.d("scalableScroll: detect scaleChange:$zoomChange, after scale:$scale")
        offset += panChange
    }
    val flingBehavior =
        rememberSnapFlingBehavior(lazyListState = state as LazyListState) as SnapFlingBehavior
    val channel = remember { Channel<ScaleScrollEvent>(Channel.UNLIMITED) }
    if (enabled) {
        LaunchedEffect(state) {
            var flingJob: Job? = null
            while (isActive) {
                val event = channel.receive()
                XLog.d("scalableScroll: $event")
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
                            XLog.d(
                                "scalableScroll: scroll by ${
                                    if (orientation == Orientation.Vertical) event.delta.y
                                    else event.delta.x
                                }"
                            )
                            state.scrollBy(
                                if (orientation == Orientation.Vertical) -event.delta.y
                                else -event.delta.x,
                            )
                        }

                        is ScaleScrollEvent.ScaleScrollStopped -> {
                            state.scroll {
                                with(flingBehavior) {
                                    flingJob = coroutineScope.launch {
                                        performFling(
                                            if (orientation == Orientation.Vertical) -event.velocity.y
                                            else -event.velocity.x,
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
                        channel.trySend(ScaleScrollEvent.ScaleScrollStopped(velocityTracker.calculateVelocity()))
                        velocityTracker.resetTracking()
                    }
                }
            }
        }
    }
    return@composed Modifier
        .pointerInput(channel, block = block)
        .scale(scale)
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