package jp.benkyosuruzo.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import jp.benkyosuruzo.R

/**
 * Speech-rate picker (0.5..2.0 in 0.1 steps). Persists a Float for TASK-03
 * (`TextToSpeech.setSpeechRate`).
 */
class SpeechRatePreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private var current = 1.0f

    override fun onAttached() {
        super.onAttached()
        updateSummary()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        current = getPersistedFloat(1.0f).coerceIn(0.5f, 2.0f)
        updateSummary()
    }

    override fun onClick() {
        val seekBar = SeekBar(context).apply {
            max = 15 // 0.5..2.0 in 0.1 steps
            progress = ((current - 0.5f) * 10f).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    current = 0.5f + progress * 0.1f
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.pref_speech_rate_title)
            .setView(seekBar)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                persistFloat(current)
                updateSummary()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateSummary() {
        summary = String.format(context.getString(R.string.pref_speech_rate_summary), current)
    }
}
