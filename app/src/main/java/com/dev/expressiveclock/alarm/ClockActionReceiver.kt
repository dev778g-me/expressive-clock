package com.dev.expressiveclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.dev.expressiveclock.core.ClockTimer
import com.dev.expressiveclock.core.Lap
import com.dev.expressiveclock.core.Stopwatch
import com.dev.expressiveclock.core.TimerState
import com.dev.expressiveclock.data.ClockStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration

class ClockActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val id = intent.getLongExtra(AlarmReceiver.EXTRA_ID, -1L)
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            val store = ClockStore(app)
            try {
                when (intent.action) {
                    ACTION_SNOOZE -> snooze(app, store, id)
                    ACTION_DISMISS -> AlarmService.stop(app)
                    ACTION_TIMER_TOGGLE -> timerToggle(app, store)
                    ACTION_TIMER_ADD -> timerAdd(app, store)
                    ACTION_TIMER_CANCEL -> {
                        AlarmService.stop(app)
                        store.putTimer(null)
                        TimerScheduler.sync(app, null)
                    }
                    ACTION_SW_TOGGLE -> stopwatchToggle(app, store)
                    ACTION_SW_LAP -> stopwatchLap(app, store)
                    ACTION_SW_RESET -> {
                        store.putStopwatch(Stopwatch())
                        Notifications.hideStopwatch(app)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun snooze(context: Context, store: ClockStore, id: Long) {
        AlarmService.stop(context)
        val minutes = store.settingsNow().alarms.snoozeMinutes
        val due = System.currentTimeMillis() + minutes * 60_000L
        val updated = store.alarmsNow().map {
            if (it.id == id) it.copy(snoozedUntilMillis = due, enabled = true) else it
        }
        store.putAlarms(updated)
        updated.firstOrNull { it.id == id }?.let { AlarmScheduler.schedule(context, it) }
    }

    private suspend fun timerToggle(context: Context, store: ClockStore) {
        val t = store.timerNow() ?: return
        val now = SystemClock.elapsedRealtime()
        val next = when (t.state) {
            TimerState.RUNNING -> t.copy(state = TimerState.PAUSED, pausedRemaining = t.remaining(now))
            else -> t.copy(state = TimerState.RUNNING, deadlineElapsedMillis = now + t.pausedRemaining.toMillis())
        }
        store.putTimer(next)
        TimerScheduler.sync(context, next)
    }

    private suspend fun timerAdd(context: Context, store: ClockStore) {
        val t = store.timerNow() ?: return
        val next: ClockTimer = when (t.state) {
            TimerState.RUNNING -> t.copy(
                total = t.total.plusMinutes(1),
                deadlineElapsedMillis = t.deadlineElapsedMillis + 60_000L,
            )
            else -> t.copy(total = t.total.plusMinutes(1), pausedRemaining = t.pausedRemaining.plusMinutes(1))
        }
        store.putTimer(next)
        TimerScheduler.sync(context, next)
    }

    private suspend fun stopwatchToggle(context: Context, store: ClockStore) {
        val sw = store.stopwatch.first()
        val now = SystemClock.elapsedRealtime()
        val next = if (sw.running) {
            sw.copy(running = false, accumulated = sw.elapsed(now))
        } else {
            sw.copy(running = true, startedAtElapsed = now)
        }
        store.putStopwatch(next)
        Notifications.showStopwatch(context, next)
    }

    private suspend fun stopwatchLap(context: Context, store: ClockStore) {
        val sw = store.stopwatch.first()
        if (!sw.running) return
        val total = sw.elapsed(SystemClock.elapsedRealtime())
        val previous = sw.laps.firstOrNull()?.total ?: Duration.ZERO
        val next = sw.copy(laps = listOf(Lap(sw.laps.size + 1, total.minus(previous), total)) + sw.laps)
        store.putStopwatch(next)
        Notifications.showStopwatch(context, next)
    }

    companion object {
        const val ACTION_SNOOZE = "com.dev.expressiveclock.SNOOZE"
        const val ACTION_DISMISS = "com.dev.expressiveclock.DISMISS"
        const val ACTION_TIMER_TOGGLE = "com.dev.expressiveclock.TIMER_TOGGLE"
        const val ACTION_TIMER_ADD = "com.dev.expressiveclock.TIMER_ADD"
        const val ACTION_TIMER_CANCEL = "com.dev.expressiveclock.TIMER_CANCEL"
        const val ACTION_SW_TOGGLE = "com.dev.expressiveclock.SW_TOGGLE"
        const val ACTION_SW_LAP = "com.dev.expressiveclock.SW_LAP"
        const val ACTION_SW_RESET = "com.dev.expressiveclock.SW_RESET"
    }
}
