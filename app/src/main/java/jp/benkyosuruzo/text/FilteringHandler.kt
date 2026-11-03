package jp.benkyosuruzo.text

import android.util.Log
import jp.benkyosuruzo.BuildConfig

/**
 * Filters a [ScreenText] down to the currently selected language before
 * delegating to the next handler in the chain.
 */
class FilteringHandler(
    private val next: ScreenTextHandler,
    private val detector: LanguageDetector = LanguageDetector(),
    private val languageProvider: LanguageProvider = FixedLanguageProvider()
) : ScreenTextHandler {

    // Read from another thread (OcrRuntime on Dispatchers.IO); AtomicInteger keeps a consistent view.
    private val _lastOutputCount = java.util.concurrent.atomic.AtomicInteger(0)
    val lastOutputCount: Int
        get() = _lastOutputCount.get()

    override fun onScreenText(screenText: ScreenText) {
        val selected = languageProvider.current()

        val kept = ArrayList<String>()
        val dropped = ArrayList<String>()
        screenText.texts.forEach { text ->
            if (detector.detect(text) == selected) kept.add(text) else dropped.add(text)
        }

        _lastOutputCount.set(kept.size)

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "language=${selected.bcp47} in=${screenText.texts.size} kept=${kept.size} dropped=${dropped.size}"
            )
            kept.forEach { Log.d(TAG, "[keep] ${it.take(MAX_LOG_TEXT_LENGTH)}") }
            dropped.forEach { Log.d(TAG, "[drop] ${it.take(MAX_LOG_TEXT_LENGTH)}") }
        }

        if (kept.isEmpty()) return

        next.onScreenText(screenText.copy(texts = kept, language = selected.bcp47))
    }

    private companion object {
        const val TAG = "ScreenTextFilter"
        const val MAX_LOG_TEXT_LENGTH = 200
    }
}
