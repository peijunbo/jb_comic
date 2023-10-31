package com.bedlier.jbcomic.ui

import android.app.Activity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bedlier.jbcomic.MyApplication
import com.bedlier.jbcomic.data.media.ImageStore
import com.bedlier.jbcomic.data.media.MediaImage
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Objects

class ImageViewModel : ViewModel() {
    companion object {
        private const val TAG = "ImageViewModel"
    }

    val imageList = mutableStateListOf<MediaImage>()
    val albums
        get() = imageList.groupSortedBy(albumSortState.value)

    var albumSortState = mutableStateOf(AlbumSortMethod())

    val imagesGroupByDate
        get() = imageList.groupBy {
            // group by date
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it.dateModified * 1000))
        }

    fun loadImageStore() {
        viewModelScope.launch(Dispatchers.IO) {
            val images = ImageStore.getMediaImages()
            // compare, if not equal, update the whole list
            if (images != imageList) {
                imageList.clear()
                imageList.addAll(images)
            }
        }
    }

    fun checkPermission() = XXPermissions.isGranted(
        MyApplication.context,
        Permission.READ_MEDIA_IMAGES
    )

    fun requestPermission(
        activity: Activity,
        onPermissionCallback: OnPermissionCallback = OnPermissionCallback { _, _ -> Unit }
    ) {
        XXPermissions
            .with(activity)
            .permission(Permission.READ_MEDIA_IMAGES)
            .request(onPermissionCallback)
    }
}

enum class SortMethod {
    NAME, SIZE, DATE
}

/**
 * Album sort state
 *
 * @param order true: asc, false: desc
 * @param sortMethod sort method [SortMethod]
 */
data class AlbumSortMethod(
    val order: Boolean = false,
    val sortMethod: SortMethod = SortMethod.NAME
)

fun List<MediaImage>.groupSortedBy(albumSortMethod: AlbumSortMethod): Map<String, List<MediaImage>> {
    val albums = this.groupBy { it.bucketName }
    return when (albumSortMethod.sortMethod) {
        SortMethod.NAME -> {
            if (albumSortMethod.order) {
                albums.toSortedMap()
            } else {
                albums.toSortedMap(reverseOrder())
            }
        }

        SortMethod.DATE -> {
            if (albumSortMethod.order) {
                albums.toSortedMap { name1, name2 ->
                    val date1 = albums[name1]?.maxBy { it.dateModified }?.dateModified ?: 0
                    val date2 = albums[name2]?.maxBy { it.dateModified }?.dateModified ?: 0
                    date1.compareTo(date2)
                }
            } else {
                albums.toSortedMap() { name1, name2 ->
                    val date1 = albums[name1]?.maxBy { it.dateModified }?.dateModified ?: 0
                    val date2 = albums[name2]?.maxBy { it.dateModified }?.dateModified ?: 0
                    date2.compareTo(date1)
                }
            }
        }

        SortMethod.SIZE -> {
            if (albumSortMethod.order) {
                albums.toSortedMap { name1, name2 ->
                    val size1 = albums[name1]?.size ?: 0
                    val size2 = albums[name2]?.size ?: 0
                    size1.compareTo(size2)
                }
            } else {
                albums.toSortedMap { name1, name2 ->
                    val size1 = albums[name1]?.size ?: 0
                    val size2 = albums[name2]?.size ?: 0
                    size2.compareTo(size1)
                }
            }
        }
    }
}