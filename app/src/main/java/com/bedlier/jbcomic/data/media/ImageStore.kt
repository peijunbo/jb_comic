package com.bedlier.jbcomic.data.media

import android.content.ContentUris
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.bedlier.jbcomic.MyApplication


class ImageStore {
    companion object {
        fun getMediaImages(): List<MediaImage> {
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )
            val query = MyApplication.context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                null
            )
            val images = mutableListOf<MediaImage>()
            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    Log.d("ImageStore", "getMediaImages: ")
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val bucketId = cursor.getLong(bucketIdColumn)
                    val bucketName = cursor.getString(bucketNameColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    images += MediaImage(
                        id = id.toInt(),
                        name = name,
                        uri = contentUri,
                        bucketId = bucketId,
                        bucketName = bucketName,
                        dateAdded = 0
                    )
                }
            }
            return images
        }
    }

}