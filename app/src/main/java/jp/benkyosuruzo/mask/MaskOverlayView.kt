package jp.benkyosuruzo.mask

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import jp.benkyosuruzo.R
import jp.benkyosuruzo.settings.SettingsKeys

/**
 * Full-screen overlay for mask mode. Draws a green band over every masked node
 * plus a red circular stop button, and consumes all touches (scroll lock).
 */
class MaskOverlayView(
    context: Context,
    private val onStop: () -> Unit
) : View(context) {

    private val controller = MaskModeController.get()
    private val offset = IntArray(2)

    private val maskPaint = Paint().apply {
        color = 0xFF2E7D32.toInt()
        style = Paint.Style.FILL
    }
    private val stopBgPaint = Paint().apply {
        color = 0xFFC62828.toInt()
        // Match the "隠" floating button's translucency (view alpha 0.8).
        alpha = STOP_BUTTON_ALPHA
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val stopTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        // Match the "隠" floating button's translucency (view alpha 0.8).
        alpha = STOP_BUTTON_ALPHA
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Convert screen coordinates to view coordinates (POC-01 found a ~81px offset).
        getLocationOnScreen(offset)

        controller.current().forEach { mask ->
            val b = mask.bounds
            canvas.drawRect(
                (b.left - offset[0]).toFloat(),
                (b.top - offset[1]).toFloat(),
                (b.right - offset[0]).toFloat(),
                (b.bottom - offset[1]).toFloat(),
                maskPaint
            )
        }
        val c = stopButtonCenter()
        canvas.drawCircle(c.x, c.y, STOP_BUTTON_RADIUS, stopBgPaint)
        canvas.drawText(context.getString(R.string.mask_stop_button_label), c.x, c.y + 14f, stopTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val c = stopButtonCenter()
            val dx = event.x - c.x
            val dy = event.y - c.y
            if (dx * dx + dy * dy <= STOP_BUTTON_RADIUS * STOP_BUTTON_RADIUS) {
                onStop()
                return true
            }

            // Convert to screen coordinates (POC-01 found a ~81px offset).
            getLocationOnScreen(offset)
            val screenX = (event.x + offset[0]).toInt()
            val screenY = (event.y + offset[1]).toInt()

            val prefs = context.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
            val applyUnit = if (prefs.getString(SettingsKeys.MASK_APPLY_UNIT, "word") == "sentence") {
                MaskModeController.Unit.SENTENCE
            } else {
                MaskModeController.Unit.WORD
            }
            val clearUnit = if (prefs.getString(SettingsKeys.MASK_CLEAR_UNIT, "word") == "sentence") {
                MaskModeController.Unit.SENTENCE
            } else {
                MaskModeController.Unit.WORD
            }
            val changed = controller.toggleAt(screenX, screenY, applyUnit, clearUnit)
            if (changed) invalidate()
        }
        return true // consume every touch (scroll lock)
    }

    private fun stopButtonCenter(): PointF {
        // Place the "終" button exactly where the "隠" floating button is.
        val screen = FloatingButtonController.buttonCenter(context)
        // Convert screen coordinates to view coordinates (POC-01 found a ~81px offset).
        getLocationOnScreen(offset)
        return PointF(
            (screen.x - offset[0]).toFloat(),
            (screen.y - offset[1]).toFloat()
        )
    }

    companion object {
        private const val STOP_BUTTON_RADIUS = 56f
        // 0.8f * 255, matching the "隠" floating button's view alpha.
        private const val STOP_BUTTON_ALPHA = 204
    }
}
