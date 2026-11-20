package app.expressiveclock.overlay

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class OverlayAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        OverlayManager.onAccessibilityServiceConnected(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        OverlayManager.onAccessibilityServiceDisconnected()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        OverlayManager.onAccessibilityServiceDisconnected()
        super.onDestroy()
    }

    companion object {
        var instance: OverlayAccessibilityService? = null
            private set
    }
}
