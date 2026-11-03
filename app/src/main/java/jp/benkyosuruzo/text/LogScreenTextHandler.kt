package jp.benkyosuruzo.text

import android.util.Log
import jp.benkyosuruzo.BuildConfig

/**
 * Phase 1 handler: writes captured screen text to Logcat.
 * Outputs nothing in release builds.
 */
class LogScreenTextHandler : ScreenTextHandler {

    override fun onScreenText(screenText: ScreenText) {
        if (!BuildConfig.DEBUG) return

        Log.d(TAG, "package=${screenText.packageName} count=${screenText.texts.size}")
        screenText.texts.forEachIndexed { index, text ->
            Log.d(TAG, "[$index] ${text.take(MAX_TEXT_LENGTH)}")
        }
    }

    private companion object {
        const val TAG = "ScreenTextReader"
        const val MAX_TEXT_LENGTH = 200
    }
}
