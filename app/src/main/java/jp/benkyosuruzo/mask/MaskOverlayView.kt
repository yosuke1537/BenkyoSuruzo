package jp.benkyosuruzo.mask

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import jp.benkyosuruzo.R

/**
 * Full-screen overlay for mask mode. Draws a green band over every masked node
 * plus a red stop button, and consumes all touches (scroll lock).
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
    private val stopPaint = Paint().apply {
        color = 0xFFC62828.toInt()
        style = Paint.Style.FILL
    }
    private val stopTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        controller.current().forEach { mask ->
            val b = mask.bounds
            canvas.drawRect(
                b.left.toFloat(),
                b.top.toFloat(),
                b.right.toFloat(),
                b.bottom.toFloat(),
                maskPaint
            )
        }
        val btn = stopButtonRect()
        canvas.drawRoundRect(btn, 16f, 16f, stopPaint)
        val baseline = btn.centerY() - (stopTextPaint.ascent() + stopTextPaint.descent()) / 2f
        canvas.drawText(context.getString(R.string.mask_overlay_stop_button), btn.left + 24f, baseline, stopTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // Convert to screen coordinates (POC-01 found a ~81px offset).
            getLocationOnScreen(offset)
            val screenX = (event.x + offset[0]).toInt()
            val screenY = (event.y + offset[1]).toInt()

            if (stopButtonRect().contains(event.x, event.y)) {
                onStop()
                return true
            }
            val changed = controller.toggleAt(screenX, screenY)
            if (changed) invalidate()
        }
        return true // consume every touch (scroll lock)
    }

    private fun stopButtonRect(): RectF {
        val size = 160f
        val margin = 24f
        return RectF(width - size - margin, margin, width - margin, margin + size)
    }
}
