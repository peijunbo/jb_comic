package com.bedlier.jbcomic.ui.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bedlier.jbcomic.R
import com.bedlier.jbcomic.ui.AlbumSortMethod
import com.bedlier.jbcomic.ui.SortMethod


/**
 * Album dialog for sorting album
 *
 * @param currentState current state of album sort
 * @param onDismissRequest dismiss dialog
 * @param onConfirm confirm dialog
 */
@Composable
fun AlbumDialog(
    currentState: AlbumSortMethod,
    onDismissRequest: () -> Unit = {},
    onConfirm: (state: AlbumSortMethod) -> Unit = { _ -> },
    modifier: Modifier = Modifier.wrapContentSize()
) {
    var order by remember {
        mutableStateOf(currentState.order)
    }
    var sortMethod by remember {
        mutableStateOf(currentState.sortMethod)
    }

    Dialog(
        onDismissRequest = {
            onDismissRequest()
        },
    ) {
        ElevatedCard(
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "Sort by: ")
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
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
                            verticalAlignment = Alignment.CenterVertically
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
                            verticalAlignment = Alignment.CenterVertically
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
                }
                Column {
                    Text(text = "Order: ")
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = order,
                                onClick = { order = true }
                            )
                            Text(text = "Asc", modifier = Modifier.clickable { order = true })
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !order,
                                onClick = { order = false }
                            )
                            Text(text = "Desc", modifier = Modifier.clickable { order = false })
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        onDismissRequest()
                    }) {
                        Text(text = stringResource(id = R.string.button_cancel))
                    }
                    TextButton(onClick = {
                        onConfirm(AlbumSortMethod(order, sortMethod))
                    }) {
                        Text(text = stringResource(id = R.string.button_confirm))
                    }
                }
            }
        }
    }

}
