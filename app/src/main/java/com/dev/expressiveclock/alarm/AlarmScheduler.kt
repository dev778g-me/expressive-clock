package com.dev.expressiveclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import com.dev.expressiveclock.core.Alarm
import java.time.ZoneId
import java.time.ZonedDateTime

object AlarmScheduler {

    fun canScheduleExact(context: Context): Boolean {
        val am = context.getSystemService<AlarmManager>() ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
    }

    fun schedule(context: Context, alarm: Alarm, zone: ZoneId = ZoneId.systemDefault()) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val next = alarm.nextFire(ZonedDateTime.now(zone))
        if (next == null) {
            cancel(context, alarm.id)
            return
        }
        val at = next.toInstant().toEpochMilli()
        val fire = firePendingIntent(context, alarm.id)
        if (canScheduleExact(context)) {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, showPendingIntent(context)), fire)
        } else {
            am.setWindow(AlarmManager.RTC_WAKEUP, at, 60_000L, fire)
        }
    }

    fun scheduleAll(context: Context, alarms: List<Alarm>) {
        alarms.forEach { schedule(context, it) }
    }

    fun cancel(context: Context, id: Long) {
        val am = context.getSystemService<AlarmManager>() ?: return
        am.cancel(firePendingIntent(context, id))
    }

    private fun firePendingIntent(context: Context, id: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            id.toInt(),
            Intent(context, AlarmReceiver::class.java)
                .setAction(AlarmReceiver.ACTION_FIRE)
                .putExtra(AlarmReceiver.EXTRA_ID, id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun showPendingIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName)!!,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
