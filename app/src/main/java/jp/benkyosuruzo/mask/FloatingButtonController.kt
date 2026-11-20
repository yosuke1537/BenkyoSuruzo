package jp.benkyosuruzo.mask

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import jp.benkyosuruzo.settings.SettingsKeys
import kotlin.math.abs

/**
 * Adds/removes the floating mask-mode launch button on the accessibility
 * service's TYPE_ACCESSIBILITY_OVERLAY layer. The button is shown only while
 * the accessibility service is connected and the setting is enabled.
 */
object FloatingButtonController {

    private const val TAG = "FloatingButton"

    /**
     * Visual radius (px) shared by the floating "遮" button and the overlay
     * "显" button so they always render at the same size.
     */
    const val BUTTON_RADIUS_PX = 56f
    private const val DEFAULT_BUTTON_Y = 200

    private var windowManager: WindowManager? = null
    private var buttonView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    @Volatile
    private var currentForeground: String? = null

    fun show(service: AccessibilityService) {
        if (buttonView != null) return
        val prefs = service.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(SettingsKeys.MASK_FLOATING_BUTTON_ENABLED, false)) return

        val wm = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = (2 * BUTTON_RADIUS_PX).toInt()
        val x = prefs.getInt(SettingsKeys.MASK_FLOATING_BUTTON_X, -1)
        val y = prefs.getInt(SettingsKeys.MASK_FLOATING_BUTTON_Y, -1)

        val params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = if (x >= 0) x else defaultX(wm, size)
            this.y = if (y >= 0) y else DEFAULT_BUTTON_Y
        }

        val view = FloatingButtonView(service)
        attachTouchHandler(view, params, wm, service)
        try {
            wm.addView(view, params)
            windowManager = wm
            buttonView = view
            layoutParams = params
        } catch (t: Throwable) {
            Log.e(TAG, "addView failed", t)
        }
    }

    fun hide() {
        buttonView?.let { runCatching { windowManager?.removeView(it) } }
        buttonView = null
        layoutParams = null
        currentForeground = null
    }

    fun isShowing(): Boolean = buttonView != null

    /**
     * Screen-space center of the floating button, honoring the remembered drag
     * position (or the default position when it has never been moved). Used by
     * the mask overlay so its "显" button appears at the same spot as "遮".
     */
    fun buttonCenter(context: Context): Point {
        val prefs = context.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = (2 * BUTTON_RADIUS_PX).toInt()
        val x = prefs.getInt(SettingsKeys.MASK_FLOATING_BUTTON_X, -1)
        val y = prefs.getInt(SettingsKeys.MASK_FLOATING_BUTTON_Y, -1)
        val left = if (x >= 0) x else defaultX(wm, size)
        val top = if (y >= 0) y else DEFAULT_BUTTON_Y
        return Point(left + size / 2, top + size / 2)
    }

    /**
     * Reconciles button visibility with the current foreground app and the
     * "勉強するぞ対象アプリ" (formerly OCR target apps) allow list.
     */
    fun syncVisibility(service: AccessibilityService, foregroundPackage: String) {
        if (currentForeground == foregroundPackage) return
        currentForeground = foregroundPackage

        val prefs = service.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(SettingsKeys.MASK_FLOATING_BUTTON_ENABLED, false)
        // "隠" is hidden while mask mode is active (the overlay's "終了" is shown instead).
        if (!enabled || MaskModeController.get().active) {
            hide()
            return
        }
        val targets = prefs.getStringSet(SettingsKeys.ALLOWED_PACKAGES, emptySet()) ?: emptySet()
        if (foregroundPackage in targets) {
            show(service)
        } else {
            hide()
        }
    }

    private fun defaultX(wm: WindowManager, size: Int): Int =
        screenWidth(wm) - size - 32

    @Suppress("DEPRECATION")
    private fun screenWidth(wm: WindowManager): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.width()
        } else {
            wm.defaultDisplay.width
        }

    private fun attachTouchHandler(
        view: View,
        params: WindowManager.LayoutParams,
        wm: WindowManager,
        service: AccessibilityService
    ) {
        val prefs = service.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
        var initialX = 0
        var initialY = 0
        var touchX = 0f
        var touchY = 0f
        var isDragging = false
        val touchSlop = ViewConfiguration.get(service).scaledTouchSlop

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - touchX
                    val dy = event.rawY - touchY
                    if (!isDragging && (abs(dx) > touchSlop || abs(dy) > touchSlop)) isDragging = true
                    if (isDragging) {
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        runCatching { wm.updateViewLayout(view, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        prefs.edit()
                            .putInt(SettingsKeys.MASK_FLOATING_BUTTON_X, params.x)
                            .putInt(SettingsKeys.MASK_FLOATING_BUTTON_Y, params.y)
                            .apply()
                    } else {
                        MaskOverlayService.start(service)
                    }
                    true
                }
                else -> false
            }
        }
    }
}
