package com.dev.expressiveclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dev.expressiveclock.alarm.Notifications
import com.dev.expressiveclock.overlay.OverlayManager
import com.dev.expressiveclock.ui.ClockApp

class MainActivity : ComponentActivity() {

    private var startTab by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        startTab = intent?.getStringExtra(Notifications.EXTRA_TAB)
        setContent { ClockApp(startTab = startTab) }
    }

    override fun onPostResume() {
        super.onPostResume()
        OverlayManager.onClockActivityForegrounded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startTab = intent.getStringExtra(Notifications.EXTRA_TAB)
    }
}
