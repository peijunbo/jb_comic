package com.bedlier.jbcomic.ui

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.DateFormat
import java.util.Date

class ImageViewModel : ViewModel() {
    companion object {
        private const val TAG = "ImageViewModel"
    }

    private val _imageList = mutableStateListOf<MediaImage>()
    private val imageMutex = Mutex()
    val imageList
        get() = _imageList.toList()
    var isImageLoading by mutableStateOf(false)
        private set
    val albums
        get() = _imageList.groupSortedBy(albumSortState.value)
    var albumSortState = mutableStateOf(AlbumSortMethod())

    private val _viewQueue = mutableStateListOf<MediaImage>()
    val viewQueue: List<MediaImage>
        get() = _viewQueue
    private val _viewIndex = mutableIntStateOf(0)
    var viewIndex: Int
        get() = _viewIndex.intValue
        set(value) {
            if (value in 0.._viewQueue.lastIndex)
            _viewIndex.intValue = value
        }

    val imagesGroupByDate
        get() = _imageList.groupBy {
            // group by date
            DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it.dateModified * 1000))
        }

    fun loadImageStore() {
        if (imageMutex.isLocked) { // already loading
            Log.d(TAG, "loadImageStore: locked")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            isImageLoading = true
            val images = ImageStore.getMediaImages()
            imageMutex.withLock {
                // compare, if not equal, update the whole list
                if (images != _imageList) {
                    _imageList.clear()
                    _imageList.addAll(images)
                }
            }
            isImageLoading = false
        }
    }

    fun addToViewQueue(image: MediaImage) {
        if (image !in _viewQueue) {
            _viewQueue.add(image)
        }
    }

    fun addToViewQueue(images: List<MediaImage>) {
        _viewQueue.addAll(images)
    }


    fun addAlbumToViewQueue(bucketId: Long) {
        val images = _imageList.filter { it.bucketId == bucketId && it !in _viewQueue }
        _viewQueue.addAll(images)
    }

    fun clearViewQueue() {
        _viewQueue.clear()
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