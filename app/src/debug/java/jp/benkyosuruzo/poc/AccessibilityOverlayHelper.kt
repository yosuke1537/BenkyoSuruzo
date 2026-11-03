package jp.benkyosuruzo.poc

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager

/**
 * Adds a TYPE_ACCESSIBILITY_OVERLAY window from an AccessibilityService's
 * context. This window type does not require SYSTEM_ALERT_WINDOW.
 */
object AccessibilityOverlayHelper {

    private var windowManager: WindowManager? = null
    private var view: View? = null

    fun show(
        service: AccessibilityService,
        alphaValue: Float,
        flags: Int,
        onTouchLog: (String) -> Unit
    ): View? {
        val wm = service.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
        val v = PocOverlayView(service, alphaValue, onTouchLog)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        )
        return try {
            wm.addView(v, params)
            windowManager = wm
            view = v
            v
        } catch (t: Throwable) {
            null
        }
    }

    fun remove() {
        runCatching { windowManager?.removeView(view) }
        windowManager = null
        view = null
    }
}
