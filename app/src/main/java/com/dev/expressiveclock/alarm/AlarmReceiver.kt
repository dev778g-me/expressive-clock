package com.dev.expressiveclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.dev.expressiveclock.core.TimerState
import com.dev.expressiveclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (intent.action) {
                    ACTION_FIRE -> fire(app, intent.getLongExtra(EXTRA_ID, -1L))
                    ACTION_TIMER_EXPIRED -> timerExpired(app)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun fire(context: Context, id: Long) {
        val store = ClockStore(context)
        val alarm = store.alarmsNow().firstOrNull { it.id == id } ?: return

        val cleared = store.alarmsNow().map { a ->
            when {
                a.id != id -> a
                a.isOneShot -> a.copy(enabled = false, snoozedUntilMillis = null)
                else -> a.copy(snoozedUntilMillis = null)
            }
        }
        store.putAlarms(cleared)
        cleared.firstOrNull { it.id == id }?.let { AlarmScheduler.schedule(context, it) }

        ContextCompat.startForegroundService(
            context,
            Intent(context, AlarmService::class.java)
                .setAction(AlarmService.ACTION_START)
                .putExtra(EXTRA_ID, alarm.id),
        )
    }

    private suspend fun timerExpired(context: Context) {
        val store = ClockStore(context)
        val timer = store.timerNow() ?: return
        store.putTimer(timer.copy(state = TimerState.FINISHED))
        Notifications.hideTimer(context)
        ContextCompat.startForegroundService(
            context,
            Intent(context, AlarmService::class.java).setAction(AlarmService.ACTION_START_TIMER),
        )
    }

    companion object {
        const val ACTION_FIRE = "com.dev.expressiveclock.FIRE"
        const val ACTION_TIMER_EXPIRED = "com.dev.expressiveclock.TIMER_EXPIRED"
        const val EXTRA_ID = "id"
    }
}
