package jp.benkyosuruzo.text

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageDetectorTest {

    private val detector = LanguageDetector()

    @Test
    fun detectsChineseSimplified() {
        assertEquals(Language.CHINESE_SIMPLIFIED, detector.detect("你好世界"))
    }

    @Test
    fun detectsJapaneseHiragana() {
        assertEquals(Language.JAPANESE, detector.detect("こんにちは"))
    }

    @Test
    fun detectsKorean() {
        assertEquals(Language.KOREAN, detector.detect("안녕하세요"))
    }

    @Test
    fun detectsEnglish() {
        assertEquals(Language.ENGLISH, detector.detect("Hello"))
    }

    @Test
    fun digitsAreUnknown() {
        assertEquals(Language.UNKNOWN, detector.detect("12345"))
    }

    @Test
    fun emptyIsUnknown() {
        assertEquals(Language.UNKNOWN, detector.detect(""))
    }

    @Test
    fun kanjiOnlyWordIsChinese() {
        // Known ambiguity: short kanji-only words are classified as Chinese.
        assertEquals(Language.CHINESE_SIMPLIFIED, detector.detect("学習中文"))
    }

    @Test
    fun mixedChineseAndEnglishIsChinese() {
        assertEquals(Language.CHINESE_SIMPLIFIED, detector.detect("Hello 你好"))
    }
}
