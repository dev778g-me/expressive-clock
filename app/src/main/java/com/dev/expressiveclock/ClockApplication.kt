package com.dev.expressiveclock

import android.app.Application
import com.dev.expressiveclock.overlay.AppLifecycleTracker
import com.dev.expressiveclock.overlay.OverlayManager

class ClockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifecycleTracker)
        OverlayManager.init(this)
    }
}
