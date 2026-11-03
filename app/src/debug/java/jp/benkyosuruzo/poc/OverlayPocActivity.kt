package jp.benkyosuruzo.poc

import android.app.Activity
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.TextView
import jp.benkyosuruzo.R
import jp.benkyosuruzo.service.ScreenTextReaderService

/**
 * Debug-only control panel to verify overlay touch consumption on a real
 * device. Lets the tester switch window type, alpha, and flags, then add or
 * remove a full-screen overlay.
 */
class OverlayPocActivity : Activity() {

    private lateinit var rgWindowType: RadioGroup
    private lateinit var rgAlpha: RadioGroup
    private lateinit var cbNotFocusable: CheckBox
    private lateinit var cbLayoutInScreen: CheckBox
    private lateinit var cbNotTouchModal: CheckBox
    private lateinit var logView: TextView

    private var appOverlayView: View? = null
    private var appWindowManager: WindowManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay_poc)

        rgWindowType = findViewById(R.id.rg_window_type)
        rgAlpha = findViewById(R.id.rg_alpha)
        cbNotFocusable = findViewById(R.id.cb_not_focusable)
        cbLayoutInScreen = findViewById(R.id.cb_layout_in_screen)
        cbNotTouchModal = findViewById(R.id.cb_not_touch_modal)
        logView = findViewById(R.id.log_view)

        findViewById<Button>(R.id.button_show).setOnClickListener { showOverlay() }
        findViewById<Button>(R.id.button_remove).setOnClickListener { removeOverlay() }
    }

    private fun showOverlay() {
        removeOverlay()

        val alpha = when (rgAlpha.checkedRadioButtonId) {
            R.id.rb_alpha_0 -> 0.0f
            R.id.rb_alpha_05 -> 0.5f
            else -> 1.0f
        }
        val flags = buildFlags()

        if (rgWindowType.checkedRadioButtonId == R.id.rb_type_application) {
            showApplicationOverlay(alpha, flags)
        } else {
            showAccessibilityOverlay(alpha, flags)
        }
    }

    private fun showApplicationOverlay(alpha: Float, flags: Int) {
        if (!Settings.canDrawOverlays(this)) {
            log("SYSTEM_ALERT_WINDOW not granted, opening settings")
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
            return
        }
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = PocOverlayView(this, alpha) { log(it) }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(view, params)
        appWindowManager = wm
        appOverlayView = view
        log("added TYPE_APPLICATION_OVERLAY alpha=$alpha flags=$flags")
    }

    private fun showAccessibilityOverlay(alpha: Float, flags: Int) {
        val service = ScreenTextReaderService.current()
        if (service == null) {
            log("ScreenTextReaderService not connected")
            return
        }
        val view = AccessibilityOverlayHelper.show(service, alpha, flags) { log(it) }
        if (view != null) {
            log("added TYPE_ACCESSIBILITY_OVERLAY alpha=$alpha flags=$flags")
        } else {
            log("failed to add TYPE_ACCESSIBILITY_OVERLAY")
        }
    }

    private fun removeOverlay() {
        appWindowManager?.let { wm ->
            appOverlayView?.let { v -> runCatching { wm.removeView(v) } }
        }
        appWindowManager = null
        appOverlayView = null
        AccessibilityOverlayHelper.remove()
    }

    private fun buildFlags(): Int {
        var flags = 0
        if (cbNotFocusable.isChecked) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        if (cbLayoutInScreen.isChecked) {
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }
        if (cbNotTouchModal.isChecked) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }
        return flags
    }

    private fun log(message: String) {
        runOnUiThread {
            logView.text = "$message\n${logView.text}"
        }
    }
}
