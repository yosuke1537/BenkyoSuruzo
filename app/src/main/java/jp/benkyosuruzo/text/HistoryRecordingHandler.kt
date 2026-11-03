package jp.benkyosuruzo.text

import android.util.Log
import jp.benkyosuruzo.BuildConfig
import jp.benkyosuruzo.data.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Records filtered screen text into the history database asynchronously, then
 * delegates to the next handler without waiting for the write to complete.
 */
class HistoryRecordingHandler(
    private val next: ScreenTextHandler,
    private val repository: HistoryRepository,
    private val scope: CoroutineScope,
    private val wasSpokenProvider: (ScreenText) -> Boolean = { true }
) : ScreenTextHandler {

    override fun onScreenText(screenText: ScreenText) {
        val wasSpoken = wasSpokenProvider(screenText)
        scope.launch {
            try {
                repository.record(screenText, wasSpoken = wasSpoken)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Failed to record screen text", e)
                }
            }
        }
        next.onScreenText(screenText)
    }

    private companion object {
        const val TAG = "HistoryRecordingHandler"
    }
}
