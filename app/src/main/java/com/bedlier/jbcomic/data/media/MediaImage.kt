package com.bedlier.jbcomic.data.media

import android.net.Uri

data class MediaImage(
    val id: Int,
    val name: String,
    val uri: Uri,
    val bucketId: Long,
    val bucketName: String,
    val dateAdded: Long,
)