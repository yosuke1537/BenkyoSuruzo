package jp.benkyosuruzo.ocr

import jp.benkyosuruzo.text.Language
import jp.benkyosuruzo.text.LanguageDetector

/**
 * Scores a single OCR block for adoption. Starts at 100 and subtracts
 * penalties; the result is clamped to 0..100.
 */
object AdoptionScore {

    fun score(blockText: String, expectedLanguageBcp47: String, detector: LanguageDetector): Int {
        var penalty = 0

        val trimmed = blockText.trim()

        if (!matches(detector.detect(blockText), expectedLanguageBcp47)) penalty += 60

        val nonLetterRatio = if (trimmed.isEmpty()) {
            1.0
        } else {
            trimmed.count { !it.isLetter() }.toDouble() / trimmed.length
        }
        if (nonLetterRatio > 0.5) penalty += 40

        if (trimmed.length < 2) penalty += 30
        if (trimmed.length > 200) penalty += 10

        return (100 - penalty).coerceIn(0, 100)
    }

    private fun matches(language: Language, bcp47: String): Boolean = when {
        bcp47.startsWith("zh") -> language == Language.CHINESE_SIMPLIFIED
        bcp47.startsWith("ja") -> language == Language.JAPANESE
        bcp47.startsWith("ko") -> language == Language.KOREAN
        bcp47.startsWith("en") -> language == Language.ENGLISH
        else -> false
    }
}
