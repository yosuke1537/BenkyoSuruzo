package jp.benkyosuruzo.ocr

import android.util.Log
import jp.benkyosuruzo.BuildConfig
import jp.benkyosuruzo.data.TextNormalizer
import jp.benkyosuruzo.text.LanguageDetector
import jp.benkyosuruzo.text.ScreenText

/**
 * Captures the screen, recognizes text with OCR, filters by adoption score,
 * and produces a [ScreenText] that flows into the existing chain.
 */
class OcrTextReader(
    private val captureManager: ScreenCaptureManager,
    private val engine: OcrEngine,
    private val settings: OcrSettingsProvider,
    private val detector: LanguageDetector,
    private val normalizer: TextNormalizer
) {

    suspend fun read(packageName: String): ScreenText? {
        val bitmap = captureManager.captureOnce(settings.captureMode()) ?: return null

        val result = try {
            if (BuildConfig.DEBUG) Log.d(TAG, "captured bitmap ${bitmap.width}x${bitmap.height}")
            engine.recognize(bitmap, settings.selectedLanguageBcp47())
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "OCR recognize failed", e)
            null
        } finally {
            // The captured image must never outlive this call.
            bitmap.recycle()
        }
        if (result == null) return null

        val language = settings.selectedLanguageBcp47()
        val threshold = settings.adoptionScoreThreshold()
        val kept = LinkedHashSet<String>()

        result.blocks.forEach { block ->
            val score = AdoptionScore.score(block.text, language, detector)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "block score=$score keep=${score >= threshold} text=${block.text.take(MAX_LOG_TEXT_LENGTH)}")
            }
            if (score >= threshold) {
                val normalized = normalizer.normalize(block.text, language)
                if (normalized.isNotEmpty()) kept.add(normalized)
            }
        }

        if (kept.isEmpty()) return null
        return ScreenText(
            packageName = packageName,
            texts = kept.toList(),
            timestampMs = System.currentTimeMillis(),
            language = language
        )
    }

    private companion object {
        const val TAG = "OcrTextReader"
        const val MAX_LOG_TEXT_LENGTH = 200
    }
}
