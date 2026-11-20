package jp.benkyosuruzo.mask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaskTargetResolverLanguageTest {

    private fun node(id: String, text: String, l: Int, t: Int, r: Int, b: Int) =
        TextNode(id, text, Bounds(l, t, r, b), "com.example.app", id)

    @Test
    fun excludedChineseIsNotMaskable() {
        val resolver = MaskTargetResolver(
            listOf(node("zh", "你好世界", 0, 0, 100, 50)),
            excludedLanguages = setOf("zh")
        )
        assertNull(resolver.findTargetAt(50, 25))
    }

    @Test
    fun nonExcludedLanguageIsMaskable() {
        val resolver = MaskTargetResolver(
            listOf(node("en", "Hello world", 0, 0, 100, 50)),
            excludedLanguages = setOf("zh")
        )
        assertEquals("en", resolver.findTargetAt(50, 25)?.id)
    }

    @Test
    fun excludedJapaneseKanaIsNotMaskable() {
        val resolver = MaskTargetResolver(
            listOf(node("ja", "こんにちは", 0, 0, 100, 50)),
            excludedLanguages = setOf("ja")
        )
        assertNull(resolver.findTargetAt(50, 25))
    }

    @Test
    fun allFiltersExcludedLanguages() {
        val resolver = MaskTargetResolver(
            listOf(
                node("zh", "你好", 0, 0, 100, 50),
                node("en", "Hello", 0, 60, 100, 110)
            ),
            excludedLanguages = setOf("zh")
        )
        assertEquals(listOf("en"), resolver.all().map { it.id })
    }

    @Test
    fun emptyExcludedSetKeepsAllMaskableNodes() {
        val resolver = MaskTargetResolver(
            listOf(
                node("zh", "你好", 0, 0, 100, 50),
                node("en", "Hello", 0, 60, 100, 110)
            )
        )
        assertEquals(setOf("zh", "en"), resolver.all().map { it.id }.toSet())
    }
}
