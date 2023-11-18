package com.bedlier.jbcomic.ui.viewer.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.elvishew.xlog.XLog

fun Modifier.partitionTap(
    totalRow: Int,
    totalColumn: Int,
    onTap: ((row: Int, column: Int) -> Unit)? = null,
    onDoubleTap: ((row: Int, column: Int) -> Unit)? = null,
    onLongPress: ((row: Int, column: Int) -> Unit)? = null
) = this.composed {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }

    val block: suspend PointerInputScope.() -> Unit = {
        detectTapGestures(
            onTap = if(onTap != null) {it ->
                val row = (it.y / (size.height / totalRow)).toInt().coerceIn(0, totalRow - 1)
                val column =
                    (it.x / (size.width / totalColumn)).toInt().coerceIn(0, totalColumn - 1)
                onTap(row, column)
            } else null,
            onDoubleTap = if (onDoubleTap !=null){ it ->
                val row = (it.y / (size.height / totalRow)).toInt().coerceIn(0, totalRow - 1)
                val column =
                    (it.x / (size.width / totalColumn)).toInt().coerceIn(0, totalColumn - 1)
                onDoubleTap(row, column)
            } else null,
            onLongPress = if (onLongPress != null) { it ->
                val row = (it.y / (size.height / totalRow)).toInt().coerceIn(0, totalRow - 1)
                val column =
                    (it.x / (size.width / totalColumn)).toInt().coerceIn(0, totalColumn - 1)
                onLongPress(row, column)
            } else null
        )
    }
    Modifier
        .onSizeChanged { size = it }
        .pointerInput(Unit, block = block)
}

fun Modifier.horizontalPartitionTap(
    onLeftTap: (() -> Unit)? = null,
    onMiddleTap: (() -> Unit)? = null,
    onRightTap: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null
) = this.partitionTap(
    totalRow = 1,
    totalColumn = 3,
    onTap = if (onLeftTap != null || onRightTap != null || onMiddleTap != null)
        { row, col ->
            XLog.d("horizontalPartitionTap: $col")
            when (col) {
                0 -> onLeftTap?.invoke()
                1 -> onMiddleTap?.invoke()
                2 -> onRightTap?.invoke()
            }
        } else null,
    onDoubleTap = if (onDoubleTap != null) { _, _ ->
        onDoubleTap()
    } else null,
    onLongPress = if (onLongPress != null) { _, _ ->
        onLongPress()
    } else null
)
