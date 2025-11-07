package com.shipthis.go

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShipThisGoApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Ensure dynamic feature splits are installed into the classloader
        SplitCompat.install(this)
    }
}