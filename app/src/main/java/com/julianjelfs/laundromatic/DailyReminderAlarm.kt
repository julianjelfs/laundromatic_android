package com.julianjelfs.laundromatic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Wakes the app once a day at [DailyReminder.reminderTime] to run [DailyReminderWorker].
 *
 * A periodic job can't be trusted with this. Once the app goes a day without being opened,
 * Android moves it to the RARE standby bucket, which allows three job sessions per 24 hours,
 * and a job that polls through the night spends them all before 9am. Alarms don't draw on
 * that quota.
 */
object DailyReminderAlarm {
    private val window = TimeUnit.MINUTES.toMillis(15)

    /**
     * Arms the next alarm and, if today's reminder hasn't gone out yet and it's past 9am,
     * runs it now. Covers the alarm firing, and a phone that was off or updating at 9am.
     */
    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        val triggerAt = DailyReminder.nextReminderAt(now)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Inexact, so no exact alarm permission. It can wait for a Doze maintenance window,
        // and Doze ends as soon as the phone is picked up.
        context.getSystemService(AlarmManager::class.java)
            .setWindow(AlarmManager.RTC_WAKEUP, triggerAt, window, pendingIntent(context))

        if (DailyReminderWorker.isDue(context, now)) DailyReminderWorker.enqueue(context)
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DailyReminderReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}

/** Receives the daily alarm, plus the system broadcasts that clear or shift alarms. */
class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        DailyReminderAlarm.schedule(context)
    }
}
