package com.dev.expressiveclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.content.getSystemService
import com.dev.expressiveclock.core.ClockTimer
import com.dev.expressiveclock.core.TimerState

object TimerScheduler {

    fun sync(context: Context, timer: ClockTimer?) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val pi = expiryIntent(context)
        am.cancel(pi)

        if (timer == null) {
            Notifications.hideTimer(context)
            return
        }
        if (timer.state == TimerState.RUNNING) {
            val at = timer.deadlineElapsedMillis
            if (at > SystemClock.elapsedRealtime()) {
                val inMillis = at - SystemClock.elapsedRealtime()
                val show = Notifications.openApp(context, Notifications.TAB_TIMERS)
                am.setAlarmClock(
                    AlarmManager.AlarmClockInfo(System.currentTimeMillis() + inMillis, show),
                    pi,
                )
            }
        }
        Notifications.showTimer(context, timer)
    }

    private fun expiryIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        900,
        Intent(context, AlarmReceiver::class.java).setAction(AlarmReceiver.ACTION_TIMER_EXPIRED),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
