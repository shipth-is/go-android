package com.shipthis.go

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import com.shipthis.go.util.SocketLogHandler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShipThisGoApplication : Application() {

    @Inject
    lateinit var socketLogHandler: SocketLogHandler

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Ensure dynamic feature splits are installed into the classloader
        SplitCompat.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize log handler after Hilt injection is ready
        LogInterceptor.setLogHandler(socketLogHandler)
        // Check for and process crash logs
        socketLogHandler.checkAndProcessCrashLogs()
    }
}