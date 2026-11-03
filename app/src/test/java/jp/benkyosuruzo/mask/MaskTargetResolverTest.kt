package jp.benkyosuruzo.mask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaskTargetResolverTest {

    private fun node(id: String, l: Int, t: Int, r: Int, b: Int) =
        TextNode(id, "text-$id", Bounds(l, t, r, b), "com.example.app")

    @Test
    fun returnsCorrectNodeForNonOverlappingNodes() {
        val resolver = MaskTargetResolver(
            listOf(
                node("a", 0, 0, 100, 50),
                node("b", 0, 100, 100, 150)
            )
        )
        assertEquals("a", resolver.findTargetAt(50, 25)?.id)
        assertEquals("b", resolver.findTargetAt(50, 125)?.id)
    }

    @Test
    fun returnsSmallestNodeWhenOverlapping() {
        val resolver = MaskTargetResolver(
            listOf(
                node("big", 0, 0, 200, 200),
                node("small", 50, 50, 100, 100)
            )
        )
        assertEquals("small", resolver.findTargetAt(75, 75)?.id)
    }

    @Test
    fun returnsNullWhenNoMatch() {
        val resolver = MaskTargetResolver(listOf(node("a", 0, 0, 100, 50)))
        assertNull(resolver.findTargetAt(500, 500))
    }

    @Test
    fun returnsNullForEmptyList() {
        val resolver = MaskTargetResolver(emptyList())
        assertNull(resolver.findTargetAt(10, 10))
    }
}
