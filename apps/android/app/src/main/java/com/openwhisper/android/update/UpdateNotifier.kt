package com.openwhisper.android.update

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.openwhisper.android.BuildConfig
import com.openwhisper.android.MainActivity
import com.openwhisper.android.R
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

object UpdateNotifier {
    private val checking = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "openwhisper-update-notifier").apply { isDaemon = true }
    }

    fun checkIfDue(context: Context) {
        val application = context.applicationContext
        val preferences = application.getSharedPreferences(PREFERENCES, 0)
        val now = System.currentTimeMillis()
        if (now - preferences.getLong(LAST_CHECK, 0) < CHECK_INTERVAL_MS) return
        if (!checking.compareAndSet(false, true)) return
        preferences.edit().putLong(LAST_CHECK, now).apply()
        executor.execute {
            try {
                AndroidUpdateClient().findUpdate(BuildConfig.VERSION_CODE)?.let { update ->
                    showNotification(application, update)
                }
            } finally {
                checking.set(false)
            }
        }
    }

    /**
     * The notification carries only a marker. Update metadata is never read from an
     * intent: MainActivity is exported, so any installed app could forge those extras.
     * The manifest is always re-fetched from the pinned feed URL instead.
     */
    fun isUpdateIntent(intent: Intent?): Boolean =
        intent?.getBooleanExtra(EXTRA_UPDATE, false) == true

    private fun showNotification(context: Context, update: UpdateManifest) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL,
                context.getString(R.string.update_notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
        val launch = PendingIntent.getActivity(
            context,
            update.versionCode,
            Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_UPDATE, true)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        manager.notify(
            NOTIFICATION_ID,
            Notification.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_openwhisper)
                .setContentTitle(context.getString(R.string.update_available_title))
                .setContentText(
                    context.getString(R.string.update_notification_message, update.versionName),
                )
                .setContentIntent(launch)
                .setAutoCancel(true)
                .build(),
        )
    }

    private const val PREFERENCES = "openwhisper_updates"
    private const val LAST_CHECK = "last_check_epoch_ms"
    private const val CHECK_INTERVAL_MS = 24L * 60L * 60L * 1000L
    private const val CHANNEL = "openwhisper_updates"
    private const val NOTIFICATION_ID = 7002
    private const val EXTRA_UPDATE = "com.openwhisper.android.extra.UPDATE"
}
