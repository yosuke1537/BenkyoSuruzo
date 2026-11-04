package jp.benkyosuruzo.mask

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import jp.benkyosuruzo.R

/**
 * Circular floating button that launches mask mode on a single tap. Drawn in
 * code (green circle + label) so no image assets are required.
 */
class FloatingButtonView(context: Context) : View(context) {

    private val bgPaint = Paint().apply {
        color = 0xFF2E7D32.toInt()
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val label = context.getString(R.string.mask_floating_button_label)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val r = minOf(width, height) / 2f
        canvas.drawCircle(width / 2f, height / 2f, r * 0.9f, bgPaint)
        canvas.drawText(label, width / 2f, height / 2f + 12f, textPaint)
    }
}
