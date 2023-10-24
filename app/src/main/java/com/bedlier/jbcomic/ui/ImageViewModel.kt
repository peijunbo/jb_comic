package com.bedlier.jbcomic.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bedlier.jbcomic.MyApplication
import com.bedlier.jbcomic.data.media.MediaImage
import com.bedlier.jbcomic.data.media.ImageStore
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewModel : ViewModel() {
    companion object {
        private const val TAG = "ImageViewModel"
    }

    val imageList = mutableStateListOf<MediaImage>()
    val imageListByBucket
        get() = imageList.groupBy { it.bucketId }
    fun loadImageStore() {
        viewModelScope.launch(Dispatchers.IO) {
            imageList.clear()
            imageList.addAll(ImageStore.getMediaImages())
            Log.d(TAG, "loadImageStore: ${imageList.toList()}")
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