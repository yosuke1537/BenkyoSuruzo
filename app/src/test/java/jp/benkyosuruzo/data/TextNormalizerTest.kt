package jp.benkyosuruzo.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun normalizesFullWidthToHalfWidth() {
        // Use a non-English language so the lowercase step does not interfere.
        assertEquals("ABC123", TextNormalizer.normalize("ＡＢＣ１２３", "ja"))
    }

    @Test
    fun collapsesRepeatedWhitespace() {
        assertEquals("a b c", TextNormalizer.normalize("a   b\tc", "en"))
    }

    @Test
    fun lowercasesEnglish() {
        assertEquals("hello world", TextNormalizer.normalize("Hello World", "en"))
    }

    @Test
    fun emptyBecomesEmpty() {
        assertEquals("", TextNormalizer.normalize("", "ja"))
    }

    @Test
    fun trimsLeadingAndTrailingWhitespace() {
        assertEquals("你好", TextNormalizer.normalize("  你好  ", "zh-Hans"))
    }
}
