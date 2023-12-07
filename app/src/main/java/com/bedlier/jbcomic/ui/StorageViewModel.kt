package com.bedlier.jbcomic.ui

import android.app.Activity
import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.bedlier.jbcomic.MainActivity
import com.bedlier.jbcomic.MyApplication
import com.elvishew.xlog.XLog
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.io.File

class StorageViewModel: ViewModel() {
    val rootDir by lazy {
        Environment.getExternalStorageDirectory()
    }
    val currentDir by lazy {
        mutableStateOf(Environment.getExternalStorageDirectory())
    }
    val currentFiles
        get() = currentDir.value.listFiles()?.toList() ?: emptyList()
    fun changeDir(dir: String) {
        currentDir.value = MyApplication.context.getExternalFilesDir(dir)
    }


    fun clickFile(file: File) {
        if (file.isDirectory) {
            currentDir.value = file
        } else {
            XLog.d("clickFile: ${file.absolutePath}")
        }
    }
    fun checkPermission() = XXPermissions.isGranted(MyApplication.context, Permission.MANAGE_EXTERNAL_STORAGE)
    fun requestPermission(
        activity: Activity,
        callback: (permissions: MutableList<String>, allGranted: Boolean) -> Unit,
        ) {
        XXPermissions.with(activity)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(callback)
    }
}