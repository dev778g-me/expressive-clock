package app.expressiveclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.expressiveclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                reschedule(app)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun reschedule(context: Context) {
        val store = ClockStore(context)
        Notifications.ensureChannels(context)
        AlarmScheduler.scheduleAll(context, store.alarmsNow())
        store.timerNow()?.let { TimerScheduler.sync(context, it) }
    }
}
