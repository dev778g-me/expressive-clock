package com.dev.expressiveclock.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.dev.expressiveclock.R
import com.dev.expressiveclock.data.ClockStore
import com.dev.expressiveclock.data.SILENT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var silenceJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Notifications.ensureChannels(this)
        when (intent?.action) {
            ACTION_START -> startRinging(intent.getLongExtra(AlarmReceiver.EXTRA_ID, -1L))
            ACTION_START_TIMER -> startTimerChime()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun startRinging(id: Long) {
        scope.launch {
            val store = ClockStore(this@AlarmService)
            val alarm = store.alarmsNow().firstOrNull { it.id == id }
            val settings = store.settingsNow()
            val label = alarm?.label?.ifBlank { null } ?: "Alarm"

            foreground(ringingNotification(this@AlarmService, id, label))
            val soundUri = alarm?.soundUri
            if (soundUri != SILENT) {
                play(soundUri?.let(Uri::parse) ?: defaultAlarmUri())
            }
            if (alarm?.vibrate != false) vibrate(ALARM_PATTERN)

            silenceJob = launch {
                val minutes = settings.alarms.silenceAfterMinutes
                if (minutes <= 0) return@launch
                delay(minutes * 60_000L)
                stopSelf()
            }
        }
    }

    private fun startTimerChime() {
        scope.launch {
            val settings = ClockStore(this@AlarmService).settingsNow()
            foreground(timerNotification(this@AlarmService))
            val soundUri = settings.timers.soundUri
            if (soundUri != SILENT) {
                play(soundUri?.let(Uri::parse) ?: defaultAlarmUri())
            }
            if (settings.timers.vibrate) vibrate(TIMER_PATTERN)
            silenceJob = launch {
                val minutes = settings.alarms.silenceAfterMinutes
                if (minutes <= 0) return@launch
                delay(minutes * 60_000L)
                stopSelf()
            }
        }
    }

    private fun foreground(n: android.app.Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(Notifications.ID_RINGING, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(Notifications.ID_RINGING, n)
        }
    }

    private fun play(uri: Uri) {
        runCatching {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
        }.onFailure {
            runCatching {
                player = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                    ?.apply { isLooping = true; start() }
            }
        }
    }

    private fun vibrate(pattern: LongArray) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService<Vibrator>()
        }
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0), attrs)
    }

    override fun onDestroy() {
        silenceJob?.cancel()
        runCatching { player?.stop() }
        player?.release()
        player = null
        vibrator?.cancel()
        scope.cancel()
        NotificationManagerCompat.from(this).cancel(Notifications.ID_RINGING)
        super.onDestroy()
    }

    private fun defaultAlarmUri(): Uri =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

    companion object {
        const val ACTION_START = "com.dev.expressiveclock.RING"
        const val ACTION_START_TIMER = "com.dev.expressiveclock.RING_TIMER"

        /** Half a second on, half off: insistent without being a klaxon. */
        private val ALARM_PATTERN = longArrayOf(0, 500, 500)
        private val TIMER_PATTERN = longArrayOf(0, 300, 200, 300, 900)

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmService::class.java))
        }

        private fun ringingNotification(context: Context, id: Long, label: String) =
            NotificationCompat.Builder(context, Notifications.CHANNEL_ALARM)
                .setSmallIcon(R.drawable.ic_stat_alarm)
                .setContentTitle(label)
                .setContentText("Alarm")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setAutoCancel(false)
                .setFullScreenIntent(AlarmRingActivity.intent(context, id, label), true)
                .setContentIntent(AlarmRingActivity.intent(context, id, label))
                .addAction(0, "Snooze", Notifications.broadcast(context, ClockActionReceiver.ACTION_SNOOZE, 10, id))
                .addAction(0, "Dismiss", Notifications.broadcast(context, ClockActionReceiver.ACTION_DISMISS, 11, id))
                .build()

        private fun timerNotification(context: Context) =
            NotificationCompat.Builder(context, Notifications.CHANNEL_ALARM)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle("Timer")
                .setContentText("Time's up")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setContentIntent(Notifications.openApp(context, Notifications.TAB_TIMERS))
                .addAction(0, "Stop", Notifications.broadcast(context, ClockActionReceiver.ACTION_TIMER_CANCEL, 24))
                .build()
    }
}
