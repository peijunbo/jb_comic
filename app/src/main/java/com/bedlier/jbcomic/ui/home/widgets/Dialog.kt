package com.bedlier.jbcomic.ui.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.home.AlbumSortState
import com.bedlier.jbcomic.ui.home.SortMethod


/**
 * Album dialog for sorting album
 *
 * @param currentState current state of album sort
 * @param onDismissRequest dismiss dialog
 * @param onConfirm confirm dialog
 */
@Composable
fun AlbumDialog(
    currentState: AlbumSortState,
    onDismissRequest: () -> Unit = {},
    onConfirm: (state: AlbumSortState) -> Unit = { _ -> },
) {
    var order by remember {
        mutableStateOf(currentState.order)
    }
    var sortMethod by remember {
        mutableStateOf(currentState.sortMethod)
    }

    Dialog(onDismissRequest = {
        onDismissRequest()
    }) {

        Card {
            Row {
                Text(text = "Sort by: ", modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.weight(1f),
                ) {
                    RadioButton(
                        selected = sortMethod == SortMethod.NAME,
                        onClick = { sortMethod = SortMethod.NAME }
                    )
                    Text(
                        text = "Name",
                        modifier = Modifier.clickable { sortMethod = SortMethod.NAME })
                }
                Row(
                    modifier = Modifier.weight(1f),
                ) {
                    RadioButton(
                        selected = sortMethod == SortMethod.SIZE,
                        onClick = { sortMethod = SortMethod.SIZE }
                    )
                    Text(
                        text = "Size",
                        modifier = Modifier.clickable { sortMethod = SortMethod.SIZE })
                }
                Row(
                    modifier = Modifier.weight(1f),
                ) {
                    RadioButton(
                        selected = sortMethod == SortMethod.DATE,
                        onClick = { sortMethod = SortMethod.DATE }
                    )
                    Text(
                        text = "Date",
                        modifier = Modifier.clickable { sortMethod = SortMethod.DATE })
                }
            }
            Row {
                Text(text = "Order: ", modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.weight(1f),
                ) {
                    RadioButton(
                        selected = order,
                        onClick = { order = true }
                    )
                    Text(text = "Asc", modifier = Modifier.clickable { order = true })
                }
                Row(
                    modifier = Modifier.weight(1f),
                ) {
                    RadioButton(
                        selected = !order,
                        onClick = { order = false }
                    )
                    Text(text = "Desc", modifier = Modifier.clickable { order = false })
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    onDismissRequest()
                }) {
                    Text(text = stringResource(id = R.string.button_cancel))
                }
                TextButton(onClick = {
                    onConfirm(AlbumSortState(order, sortMethod))
                }) {
                    Text(text = stringResource(id = R.string.button_confirm))
                }
            }
        }
    }
}