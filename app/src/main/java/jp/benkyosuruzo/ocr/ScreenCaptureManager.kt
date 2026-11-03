package jp.benkyosuruzo.ocr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper

/**
 * Owns MediaProjection permission state and captures single frames.
 *
 * ONCE mode starts a fresh capture each time and releases it immediately.
 * PERSISTENT mode reuses a live session and falls back to a fresh capture when
 * none is active.
 */
class ScreenCaptureManager(private val context: Context) {

    private val projectionManager = context.getSystemService(MediaProjectionManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile private var resultCode: Int = Activity.RESULT_CANCELED
    @Volatile private var resultData: Intent? = null

    fun createPermissionIntent(): Intent = projectionManager.createScreenCaptureIntent()

    fun onPermissionResult(code: Int, data: Intent) {
        resultCode = code
        resultData = data
    }

    fun captureOnce(mode: CaptureMode): Bitmap? {
        val live = CaptureSession.current()
        if (mode == CaptureMode.PERSISTENT && live != null && live.isAlive) {
            return try {
                live.acquireFrame()
            } catch (_: Exception) {
                null
            }
        }

        val code = resultCode
        val data = resultData
        if (code != Activity.RESULT_OK || data == null) {
            requestPermission()
            return null
        }

        return startAndCapture(code, data, persistent = mode == CaptureMode.PERSISTENT)
    }

    private fun requestPermission() {
        mainHandler.post {
            try {
                val intent = Intent(context, OcrPermissionActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    private fun startAndCapture(code: Int, data: Intent, persistent: Boolean): Bitmap? {
        val serviceIntent = Intent(context, CaptureForegroundService::class.java)
            .putExtra(CaptureForegroundService.EXTRA_RESULT_CODE, code)
            .putExtra(CaptureForegroundService.EXTRA_RESULT_DATA, data)
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        val session = CaptureSession.awaitReady(5000) ?: return null
        return try {
            session.acquireFrame()
        } finally {
            if (!persistent) {
                context.stopService(serviceIntent)
            }
        }
    }

    fun release() {
        CaptureSession.release()
        try {
            context.stopService(Intent(context, CaptureForegroundService::class.java))
        } catch (_: Exception) {
        }
    }
}
