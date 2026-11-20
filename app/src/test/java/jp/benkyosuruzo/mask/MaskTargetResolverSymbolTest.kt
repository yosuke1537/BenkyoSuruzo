package jp.benkyosuruzo.mask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaskTargetResolverSymbolTest {

    private fun node(id: String, text: String, l: Int, t: Int, r: Int, b: Int) =
        TextNode(id, text, Bounds(l, t, r, b), "com.example.app", id)

    @Test
    fun symbolOnlyNodesAreNotMaskable() {
        val symbols = listOf(".", ",", "'", "\"", "。", "、", "！", "？", "「", "」", "『", "』", "（", "）")
        symbols.forEach { symbol ->
            val resolver = MaskTargetResolver(listOf(node("n", symbol, 0, 0, 100, 50)))
            assertNull("symbol '$symbol' should not be maskable", resolver.findTargetAt(50, 25))
        }
    }

    @Test
    fun wordWithTrailingSymbolIsMaskable() {
        val resolver = MaskTargetResolver(
            listOf(
                node("a", "你好。", 0, 0, 100, 50),
                node("b", "Hello,", 0, 60, 100, 110),
                node("c", "（重要）", 0, 120, 100, 170)
            )
        )
        assertEquals("a", resolver.findTargetAt(50, 25)?.id)
        assertEquals("b", resolver.findTargetAt(50, 85)?.id)
        assertEquals("c", resolver.findTargetAt(50, 145)?.id)
    }

    @Test
    fun ellipsisAndDashOnlyAreNotMaskable() {
        val resolver = MaskTargetResolver(
            listOf(
                node("a", "…", 0, 0, 100, 50),
                node("b", "—", 0, 60, 100, 110)
            )
        )
        assertNull(resolver.findTargetAt(50, 25))
        assertNull(resolver.findTargetAt(50, 85))
    }

    @Test
    fun overlappingWordWinsOverSymbolNode() {
        val resolver = MaskTargetResolver(
            listOf(
                node("symbol", "。", 0, 0, 200, 200),
                node("word", "你好", 50, 50, 100, 100)
            )
        )
        assertEquals("word", resolver.findTargetAt(75, 75)?.id)
    }
}
