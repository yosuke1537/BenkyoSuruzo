package jp.benkyosuruzo.text

import android.content.Context
import jp.benkyosuruzo.BenkyoSuruzoApplication
import jp.benkyosuruzo.data.AppDatabase
import jp.benkyosuruzo.data.HistoryRepository
import jp.benkyosuruzo.ocr.OcrRuntime

/**
 * Sink for captured screen text.
 *
 * This is the seam between the service and the processing chain, assembled in
 * [create]: TASK-02 filters by language, TASK-05 records history, TASK-08 runs
 * OCR fallback via [OcrRuntime], and TASK-03 will add TextToSpeech.
 */
interface ScreenTextHandler {
    fun onScreenText(screenText: ScreenText)

    companion object {
        fun create(context: Context): ScreenTextHandler {
            val app = context.applicationContext as BenkyoSuruzoApplication
            val db = AppDatabase.getInstance(context)
            val repository = HistoryRepository(db)

            // Filter + log only; used by OcrRuntime to feed OCR results.
            val logChain: ScreenTextHandler = FilteringHandler(next = LogScreenTextHandler())
            // Filter + history + log; the main accessibility pipeline.
            val historyChain: ScreenTextHandler = FilteringHandler(
                next = HistoryRecordingHandler(
                    next = LogScreenTextHandler(),
                    repository = repository,
                    scope = app.applicationScope
                )
            )

            OcrRuntime.initialize(context, app.applicationScope, logChain)
            return historyChain
        }
    }
}



