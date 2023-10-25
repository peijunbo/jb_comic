package com.bedlier.jbcomic.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
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

class ImageViewModel : ViewModel() {
    companion object {
        private const val TAG = "ImageViewModel"
    }

    val imageList = mutableStateListOf<MediaImage>()
    val albums
        get() = imageList.groupBy { it.bucketId }

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

    fun requestPermission(onPermissionCallback: OnPermissionCallback = OnPermissionCallback { _, _ -> Unit }) {
        MyApplication.currentActivity?.let { activity ->
            XXPermissions
                .with(activity)
                .permission(Permission.READ_MEDIA_IMAGES)
                .request(onPermissionCallback)
        }
    }
}