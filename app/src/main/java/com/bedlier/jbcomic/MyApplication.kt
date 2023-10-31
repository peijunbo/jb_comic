package com.bedlier.jbcomic

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context

class MyApplication: Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}