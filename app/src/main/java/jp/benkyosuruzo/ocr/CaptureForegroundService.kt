package jp.benkyosuruzo.ocr

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import jp.benkyosuruzo.R

/**
 * Foreground service required by Android 14+ to hold a MediaProjection while
 * capturing. Sets up the VirtualDisplay + ImageReader and exposes frames via
 * [CaptureSession].
 */
class CaptureForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val code = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        val data: Intent? = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            @Suppress("DEPRECATION") intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        }

        if (code == Activity.RESULT_OK && data != null) {
            setUpCapture(code, data)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        CaptureSession.release()
        super.onDestroy()
    }

    private fun setUpCapture(code: Int, data: Intent) {
        try {
            val projectionManager = getSystemService(MediaProjectionManager::class.java)
            val projection = projectionManager.getMediaProjection(code, data) ?: return

            // Android 14+: a callback must be registered before starting capture.
            projection.registerCallback(object : android.media.projection.MediaProjection.Callback() {
                override fun onStop() {
                    CaptureSession.release()
                }
            }, Handler(Looper.getMainLooper()))

            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getRealMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            val display = projection.createVirtualDisplay(
                "BenkyoSuruzoCapture",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                null
            ) ?: run {
                reader.close()
                projection.stop()
                return
            }

            CaptureSession.markReady(CaptureSession.Session(projection, reader, display))
        } catch (_: Exception) {
            CaptureSession.release()
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "ocr_capture"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                channelId,
                getString(R.string.ocr_capture_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        return Notification.Builder(this, channelId)
            .setContentTitle(getString(R.string.ocr_capture_notification_title))
            .setContentText(getString(R.string.ocr_capture_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()
    }

    companion object {
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        private const val NOTIFICATION_ID = 1001
    }
}
