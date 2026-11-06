package jp.benkyosuruzo.mask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SentenceGrouperTest {

    private fun raw(
        id: String,
        text: String,
        l: Int,
        t: Int,
        r: Int,
        b: Int,
        parent: String,
        sentence: String? = null
    ) = SentenceGrouper.RawNode(id, text, Bounds(l, t, r, b), "com.example.app", sentence, parent)

    @Test
    fun nodesUnderSentenceContainerShareSentenceKey() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "但是", 0, 0, 100, 50, parent = "main_container", sentence = "sentence:0"),
                raw("b", "在", 100, 0, 200, 50, parent = "main_container", sentence = "sentence:0"),
                raw("c", "中国", 200, 0, 300, 50, parent = "main_container", sentence = "sentence:0")
            )
        )
        assertEquals(listOf("sentence:0", "sentence:0", "sentence:0"), result.map { it.groupId })
    }

    @Test
    fun fallbackGroupsVerticallyCloseSiblings() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "hello", 0, 0, 100, 50, parent = "p"),
                raw("b", "world", 0, 50, 100, 100, parent = "p")
            )
        )
        assertEquals(result[0].groupId, result[1].groupId)
    }

    @Test
    fun fallbackBreaksGroupAtEndingPunctuation() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "hello", 0, 0, 100, 50, parent = "p"),
                raw("b", "world.", 0, 50, 100, 100, parent = "p"),
                raw("c", "next", 0, 100, 100, 150, parent = "p")
            )
        )
        assertEquals(result[0].groupId, result[1].groupId)
        assertNotEquals(result[1].groupId, result[2].groupId)
    }

    @Test
    fun fallbackBreaksGroupAtLargeVerticalGap() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "hello", 0, 0, 100, 50, parent = "p"),
                raw("b", "world", 0, 200, 100, 250, parent = "p")
            )
        )
        assertNotEquals(result[0].groupId, result[1].groupId)
    }

    @Test
    fun fallbackSeparatesDifferentParents() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "hello", 0, 0, 100, 50, parent = "p1"),
                raw("b", "world", 0, 50, 100, 100, parent = "p2")
            )
        )
        assertNotEquals(result[0].groupId, result[1].groupId)
    }
}
