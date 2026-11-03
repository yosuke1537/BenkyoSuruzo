package jp.benkyosuruzo.ocr

import jp.benkyosuruzo.data.HistoryRepository
import jp.benkyosuruzo.data.TextNormalizer
import jp.benkyosuruzo.text.ScreenText

/**
 * Records OCR provenance on the history entries matching a [ScreenText].
 * Implemented as an extension so the TASK-05 data layer stays unchanged.
 */
suspend fun HistoryRepository.applyOcrOrigin(screenText: ScreenText) {
    val language = screenText.language ?: return

    screenText.texts.forEach { text ->
        val normalized = TextNormalizer.normalize(text, language)
        if (normalized.isBlank()) return@forEach

        val entry = getByKey(language, normalized) ?: return@forEach
        upsertAttribute(entry.id, "ocr", "origin", "STRING", "ocr", null, 0)
        upsertAttribute(entry.id, "ocr", "engine", "STRING", "mlkit", null, 0)
        upsertAttribute(entry.id, "ocr", "model_language", "STRING", language, null, 0)
    }
}
