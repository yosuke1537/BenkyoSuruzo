package jp.benkyosuruzo.mask

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import jp.benkyosuruzo.R
import jp.benkyosuruzo.settings.SettingsKeys

/**
 * Owns the persistent "mask mode" notification and keeps it in sync with the
 * current state (ready / running / hidden).
 */
object MaskNotificationHelper {

    private const val CHANNEL_ID = MaskOverlayService.CHANNEL_ID
    private const val NOTIFICATION_ID = MaskOverlayService.NOTIFICATION_ID

    /**
     * Reconciles the notification with the current mask-feature setting and
     * notification permission. When disabled or unpermitted, cancels it.
     */
    fun sync(context: Context, running: Boolean = false) {
        val prefs = context.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(SettingsKeys.MASK_FEATURE_ENABLED, false)
        if (!enabled) {
            cancel(context)
            return
        }
        if (!hasNotificationPermission(context)) {
            cancel(context)
            return
        }
        show(context, running)
    }

    fun show(context: Context, running: Boolean) {
        createChannel(context)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(context, running))
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }

    fun buildNotification(context: Context, running: Boolean): Notification {
        createChannel(context)
        val startIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, MaskOverlayService::class.java).setAction(MaskOverlayService.ACTION_START),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            context,
            1,
            Intent(context, MaskOverlayService::class.java).setAction(MaskOverlayService.ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(context.getString(R.string.mask_notification_title))
            .setContentText(
                context.getString(
                    if (running) R.string.mask_notification_running else R.string.mask_notification_ready
                )
            )
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
        if (running) {
            builder.addAction(0, context.getString(R.string.mask_action_stop), stopIntent)
        } else {
            builder.addAction(0, context.getString(R.string.mask_action_start), startIntent)
        }
        return builder.build()
    }

    fun hasNotificationPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.mask_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }
}
