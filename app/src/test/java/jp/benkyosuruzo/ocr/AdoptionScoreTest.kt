package jp.benkyosuruzo.ocr

import jp.benkyosuruzo.text.LanguageDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdoptionScoreTest {

    private val detector = LanguageDetector()

    @Test
    fun matchingLanguageAndCleanTextScoresHigh() {
        assertTrue(AdoptionScore.score("你好世界", "zh-Hans", detector) >= 90)
    }

    @Test
    fun mismatchedLanguageScoresLow() {
        // "Hello" is detected as English, but the expected language is Chinese.
        assertTrue(AdoptionScore.score("Hello", "zh-Hans", detector) < 50)
    }

    @Test
    fun symbolAndDigitHeavyTextScoresLow() {
        assertTrue(AdoptionScore.score("12345...", "zh-Hans", detector) < 50)
    }

    @Test
    fun singleCharacterScoresLow() {
        assertTrue(AdoptionScore.score("中", "zh-Hans", detector) < 90)
    }

    @Test
    fun emptyTextScoresZero() {
        assertEquals(0, AdoptionScore.score("", "zh-Hans", detector))
    }
}
