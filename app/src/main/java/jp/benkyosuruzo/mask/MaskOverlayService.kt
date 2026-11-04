package jp.benkyosuruzo.mask

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import jp.benkyosuruzo.R
import jp.benkyosuruzo.service.ScreenTextReaderService

/**
 * Foreground service that owns the full-screen mask overlay. The overlay is a
 * TYPE_ACCESSIBILITY_OVERLAY window added from the accessibility service's
 * context (it cannot be added from this service's own context).
 */
class MaskOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: MaskOverlayView? = null
    private var destroyed = false
    private var shadePollAttempts = 0
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // Android 14+ 5-second rule: enter foreground immediately.
                startForeground(
                    NOTIFICATION_ID,
                    MaskNotificationHelper.buildNotification(this, running = true)
                )
                if (overlayView == null) startOverlay()
            }
        }
        return START_NOT_STICKY
    }

    private fun startOverlay() {
        // The mask starts from the notification shade, which may still be open.
        // Wait until the shade closes (the foreground app becomes the active
        // window) before adding the full-screen overlay, so the overlay does
        // not trap the shade open.
        shadePollAttempts = 0
        startOverlayWhenReady()
    }

    private fun startOverlayWhenReady() {
        if (destroyed) return
        FloatingButtonController.hide()
        val service = ScreenTextReaderService.current()
        if (service == null) {
            Toast.makeText(this, getString(R.string.mask_need_accessibility), Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }
        val activePkg = service.rootInActiveWindow?.packageName?.toString()
        if (activePkg == null || activePkg == SYSTEM_UI_PACKAGE) {
            // The notification shade is still open; retry shortly.
            if (shadePollAttempts++ < MAX_SHADE_POLL_ATTEMPTS) {
                mainHandler.postDelayed({ startOverlayWhenReady() }, SHADE_POLL_INTERVAL_MS)
                return
            }
            Toast.makeText(this, getString(R.string.mask_no_targets), Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }
        val accessNodes = service.snapshotTextNodes()
        val pkg = accessNodes.firstOrNull()?.packageName?.takeIf { it.isNotBlank() } ?: activePkg
        MaskModeController.get().start(accessNodes, pkg)
        Log.d(TAG, "start mode, nodes=${accessNodes.size} pkg=$pkg")
        addOverlayView()
        MaskNotificationHelper.sync(this, running = true)
    }

    private fun addOverlayView() {
        if (overlayView != null) return
        // TYPE_ACCESSIBILITY_OVERLAY must be added from the accessibility
        // service's context, not from this service's own context.
        val service = ScreenTextReaderService.current()
        if (service == null) {
            Toast.makeText(this, getString(R.string.mask_need_accessibility), Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }
        windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = MaskOverlayView(service) { stopSelf() }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (t: Throwable) {
            Log.e(TAG, "add overlay failed", t)
            stopSelf()
        }
    }

    override fun onDestroy() {
        destroyed = true
        // Explicitly remove the overlay. TYPE_ACCESSIBILITY_OVERLAY is tied to
        // the accessibility service's window token, so the system also removes
        // it automatically when that service disconnects; guard against the
        // already-removed case with runCatching.
        overlayView?.let { view ->
            if (::windowManager.isInitialized) {
                runCatching { windowManager.removeView(view) }
            }
        }
        overlayView = null
        MaskModeController.get().stop()
        Log.d(TAG, "stop mode")
        MaskNotificationHelper.sync(this, running = false)
        ScreenTextReaderService.current()?.let { FloatingButtonController.show(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "jp.benkyosuruzo.mask.START"
        const val ACTION_STOP = "jp.benkyosuruzo.mask.STOP"
        const val CHANNEL_ID = "mask_overlay"
        const val NOTIFICATION_ID = 2001
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        private const val SHADE_POLL_INTERVAL_MS = 200L
        private const val MAX_SHADE_POLL_ATTEMPTS = 25
        private const val TAG = "MaskOverlayService"

        fun start(context: Context) {
            runCatching {
                context.startForegroundService(
                    Intent(context, MaskOverlayService::class.java).setAction(ACTION_START)
                )
            }
        }

        fun stop(context: Context) {
            runCatching {
                context.startService(
                    Intent(context, MaskOverlayService::class.java).setAction(ACTION_STOP)
                )
            }
        }
    }
}
