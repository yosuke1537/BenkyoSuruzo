package jp.benkyosuruzo.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import jp.benkyosuruzo.R

/**
 * Adoption-score threshold picker (0..100). Persists an Int and shows the
 * current value in its summary.
 */
class ThresholdPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private var current = 50

    override fun onAttached() {
        super.onAttached()
        updateSummary()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        current = getPersistedInt(50).coerceIn(0, 100)
        updateSummary()
    }

    override fun onClick() {
        val seekBar = SeekBar(context).apply {
            max = 100
            progress = current
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    current = progress
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.pref_threshold_title)
            .setView(seekBar)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                persistInt(current)
                updateSummary()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateSummary() {
        summary = context.getString(R.string.pref_threshold_summary, current)
    }
}
