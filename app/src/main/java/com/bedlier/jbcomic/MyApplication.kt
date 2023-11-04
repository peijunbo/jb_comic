package com.bedlier.jbcomic

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.ConsolePrinter

class MyApplication: Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

    }
    override fun onCreate() {
        super.onCreate()
        val logConfiguration = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .enableBorder()
            .build()
        XLog.init(logConfiguration)

        context = applicationContext
    }
}