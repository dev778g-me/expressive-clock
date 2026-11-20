package app.expressiveclock.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.expressiveclock.data.ClockSettings
import app.expressiveclock.data.ClockStore
import app.expressiveclock.ui.rememberWallTicker
import app.expressiveclock.ui.screens.WidePill
import app.expressiveclock.ui.theme.ClockFace
import app.expressiveclock.ui.theme.ClockTheme
import app.expressiveclock.ui.theme.Numerals
import java.time.LocalTime

class AlarmRingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val id = intent.getLongExtra(AlarmReceiver.EXTRA_ID, -1L)
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()
        val store = ClockStore(applicationContext)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = Unit
        })

        setContent {
            val settings by store.settings
                .collectAsStateWithLifecycle(initialValue = ClockSettings())
            ClockTheme(settings.theme) {
                Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
                    Ringing(
                        label = label,
                        snoozeMinutes = settings.alarms.snoozeMinutes,
                        onSnooze = {
                            sendBroadcast(action(ClockActionReceiver.ACTION_SNOOZE, id))
                            finish()
                        },
                        onDismiss = {
                            sendBroadcast(action(ClockActionReceiver.ACTION_DISMISS, id))
                            finish()
                        },
                    )
                }
            }
        }
    }

    private fun action(name: String, id: Long) =
        Intent(this, ClockActionReceiver::class.java)
            .setPackage(packageName)
            .setAction(name)
            .putExtra(AlarmReceiver.EXTRA_ID, id)

    companion object {
        const val EXTRA_LABEL = "label"

        fun intent(context: Context, id: Long, label: String): PendingIntent =
            PendingIntent.getActivity(
                context,
                id.toInt(),
                Intent(context, AlarmRingActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(AlarmReceiver.EXTRA_ID, id)
                    .putExtra(EXTRA_LABEL, label),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
    }
}

@Composable
private fun Ringing(
    label: String,
    snoozeMinutes: Int,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    val now by rememberWallTicker()
    val time = LocalTime.now()
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    val formattedTime = if (is24Hour) {
        "%02d:%02d".format(time.hour, time.minute)
    } else {
        "%02d:%02d".format((time.hour % 12).takeIf { it != 0 } ?: 12, time.minute)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            label.ifBlank { "Alarm" },
            style = MaterialTheme.typography.headlineMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        Numerals(
            text = formattedTime,
            capHeight = 132.dp,
            color = MaterialTheme.colorScheme.primary,
            width = ClockFace.CONDENSED,
            weight = ClockFace.WEIGHT_ON,
            tracking = ClockFace.CONDENSED_TRACKING,
        )
        Spacer(Modifier.height(56.dp))
        WidePill(
            text = "Dismiss",
            onClick = onDismiss,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
            height = 96.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        WidePill(
            text = "Snooze $snoozeMinutes min",
            onClick = onSnooze,
            outlined = true,
            content = MaterialTheme.colorScheme.onSurface,
            height = 72.dp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
