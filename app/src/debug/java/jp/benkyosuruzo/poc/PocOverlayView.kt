package jp.benkyosuruzo.poc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * Full-screen overlay view for the touch-consumption PoC. Draws a green fill
 * (alpha-controlled) and consumes every touch event.
 */
class PocOverlayView(
    context: Context,
    private val alphaValue: Float,
    private val onTouchLog: (String) -> Unit
) : View(context) {

    private val paint = Paint().apply {
        color = 0xFF2E7D32.toInt()
        this.alpha = (alphaValue * 255).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PoC Overlay", width / 2f, height / 2f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        onTouchLog("action=${event.action} x=${event.x} y=${event.y}")
        Log.d(TAG, "touch action=${event.action} x=${event.x} y=${event.y}")
        return true // consume every touch (scroll lock)
    }

    companion object {
        private const val TAG = "PocOverlay"
    }
}
