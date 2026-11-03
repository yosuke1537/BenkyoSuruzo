package jp.benkyosuruzo.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilteringHandlerTest {

    private class RecordingHandler : ScreenTextHandler {
        val received = mutableListOf<ScreenText>()
        override fun onScreenText(screenText: ScreenText) {
            received.add(screenText)
        }
    }

    @Test
    fun keepsOnlySelectedLanguage() {
        val next = RecordingHandler()
        val handler = FilteringHandler(
            next = next,
            detector = LanguageDetector(),
            languageProvider = FixedLanguageProvider(Language.CHINESE_SIMPLIFIED)
        )

        handler.onScreenText(
            ScreenText(
                packageName = "com.example.app",
                texts = listOf("你好世界", "こんにちは", "Hello", "学习中文"),
                timestampMs = 0L
            )
        )

        assertEquals(1, next.received.size)
        assertEquals(listOf("你好世界", "学习中文"), next.received[0].texts)
        assertEquals("zh-Hans", next.received[0].language)
    }

    @Test
    fun doesNotCallNextWhenAllDropped() {
        val next = RecordingHandler()
        val handler = FilteringHandler(
            next = next,
            detector = LanguageDetector(),
            languageProvider = FixedLanguageProvider(Language.CHINESE_SIMPLIFIED)
        )

        handler.onScreenText(
            ScreenText(
                packageName = "com.example.app",
                texts = listOf("こんにちは", "Hello", "12345"),
                timestampMs = 0L
            )
        )

        assertTrue(next.received.isEmpty())
    }

    @Test
    fun setsLanguageAfterFiltering() {
        val next = RecordingHandler()
        val handler = FilteringHandler(
            next = next,
            detector = LanguageDetector(),
            languageProvider = FixedLanguageProvider(Language.ENGLISH)
        )

        handler.onScreenText(
            ScreenText(
                packageName = "com.example.app",
                texts = listOf("Hello", "World"),
                timestampMs = 0L
            )
        )

        assertEquals(1, next.received.size)
        assertEquals("en", next.received[0].language)
    }
}
