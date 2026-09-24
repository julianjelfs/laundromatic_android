package com.julianjelfs.laundromatic

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Fetches items and posts the reminder. [DailyReminderAlarm] enqueues it each morning.
 * It posts at most once per day, so a duplicate run returns straight away.
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val prefs = prefs(applicationContext)
        val lastNotifiedOn = lastNotifiedOn(prefs)
        val now = LocalDateTime.now()
        Log.d(TAG, "Running at $now, last notified on $lastNotifiedOn")

        if (!DailyReminder.isDue(now, lastNotifiedOn)) return Result.success()

        // Don't mark the day as done while notifications are off, or granting permission
        // later that day would still leave you without a reminder until tomorrow.
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            Log.d(TAG, "Notifications are off, skipping")
            return Result.success()
        }

        val userId = Firebase.auth.currentUser?.uid ?: run {
            Log.d(TAG, "Not signed in, skipping")
            return Result.success()
        }
        val repository = (applicationContext as LaundromaticApplication).repository

        val items = try {
            repository.refreshItems(userId)
        } catch (e: Exception) {
            Log.w(TAG, "Fetching items failed, will retry", e)
            return Result.retry()
        }

        val reminder = DailyReminder.build(items)
        Log.d(TAG, "Fetched ${items.size} items, reminder: ${reminder?.title}")
        reminder?.let(::post)
        prefs.edit().putString(KEY_LAST_NOTIFIED_ON, now.toLocalDate().toString()).apply()
        return Result.success()
    }

    // Only used below Android 12, where expedited work runs as a foreground service.
    override suspend fun getForegroundInfo(): ForegroundInfo =
        ForegroundInfo(
            FOREGROUND_NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Checking your laundry")
                .setSilent(true)
                .build(),
        )

    private fun post(reminder: Reminder) {
        val notifications = NotificationManagerCompat.from(applicationContext)

        val openApp = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val style = NotificationCompat.InboxStyle()
        reminder.lines.forEach(style::addLine)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.title)
            .setContentText(reminder.lines.joinToString(" · "))
            .setStyle(style)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        try {
            notifications.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS revoked between the check and the call.
        }
    }

    companion object {
        private const val TAG = "DailyReminder"
        private const val WORK_NAME = "daily-reminder-run"
        private const val LEGACY_PERIODIC_WORK_NAME = "daily-reminder"
        private const val CHANNEL_ID = "daily-reminder"
        private const val NOTIFICATION_ID = 1
        private const val FOREGROUND_NOTIFICATION_ID = 2
        private const val PREFS_NAME = "daily-reminder"
        private const val KEY_LAST_NOTIFIED_ON = "lastNotifiedOn"

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily reminder",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Morning summary of overdue and upcoming laundry"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        private fun prefs(context: Context) =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        private fun lastNotifiedOn(prefs: SharedPreferences): LocalDate? =
            prefs.getString(KEY_LAST_NOTIFIED_ON, null)?.let(LocalDate::parse)

        fun isDue(context: Context, now: LocalDateTime): Boolean =
            DailyReminder.isDue(now, lastNotifiedOn(prefs(context)))

        /** Expedited so it runs straight away, even when the app is in a low standby bucket. */
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        /** Builds before the alarm polled every 30 minutes under this name. */
        fun cancelLegacyPeriodicWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(LEGACY_PERIODIC_WORK_NAME)
        }
    }
}
