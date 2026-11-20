package jp.benkyosuruzo.mask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SentenceGrouperEnglishTest {

    private fun raw(id: String, text: String, l: Int, t: Int, r: Int, b: Int, parent: String = "p") =
        SentenceGrouper.RawNode(id, text, Bounds(l, t, r, b), "com.example.app", null, parent)

    @Test
    fun englishSentenceEndsAreRecognized() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "Hello world.", 0, 0, 100, 50),
                raw("b", "How are you?", 0, 50, 100, 100),
                raw("c", "Nice!", 0, 100, 100, 150)
            )
        )
        // Each ends with a sentence terminator, so each is its own group.
        assertNotEquals(result[0].groupId, result[1].groupId)
        assertNotEquals(result[1].groupId, result[2].groupId)
    }

    @Test
    fun commaIsNotASentenceEnd() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "Hello,", 0, 0, 100, 50),
                raw("b", "world", 0, 50, 100, 100)
            )
        )
        assertEquals(result[0].groupId, result[1].groupId)
    }

    @Test
    fun japaneseSentenceEndStillWorks() {
        val result = SentenceGrouper.assign(
            listOf(
                raw("a", "你好。", 0, 0, 100, 50),
                raw("b", "next", 0, 50, 100, 100)
            )
        )
        assertNotEquals(result[0].groupId, result[1].groupId)
    }
}
