package jp.benkyosuruzo.mask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaskModeControllerTest {

    private fun controller() = MaskModeController.get()

    private fun node(id: String, l: Int, t: Int, r: Int, b: Int, groupId: String = id) =
        TextNode(id, "text-$id", Bounds(l, t, r, b), "com.example.app", groupId)

    private fun sentenceNodes() = listOf(
        node("a", 0, 0, 100, 50, groupId = "g1"),
        node("b", 100, 0, 200, 50, groupId = "g1"),
        node("c", 200, 0, 300, 50, groupId = "g1"),
        node("d", 0, 100, 100, 150, groupId = "g2")
    )

    @Test
    fun toggleAtAddsMaskAfterStart() {
        val ctrl = controller()
        ctrl.start(listOf(node("a", 0, 0, 100, 50)), "com.example.app")
        assertTrue(ctrl.toggleAt(50, 25))
        assertEquals(1, ctrl.current().size)
        assertEquals("a", ctrl.current()[0].nodeId)
        ctrl.stop()
    }

    @Test
    fun toggleAtRemovesExistingMask() {
        val ctrl = controller()
        ctrl.start(listOf(node("a", 0, 0, 100, 50)), "com.example.app")
        ctrl.toggleAt(50, 25)
        assertTrue(ctrl.toggleAt(50, 25))
        assertTrue(ctrl.current().isEmpty())
        ctrl.stop()
    }

    @Test
    fun stopClearsMasksAndDeactivates() {
        val ctrl = controller()
        ctrl.start(listOf(node("a", 0, 0, 100, 50)), "com.example.app")
        ctrl.toggleAt(50, 25)
        ctrl.stop()
        assertTrue(ctrl.current().isEmpty())
        assertFalse(ctrl.active)
        assertNull(ctrl.originPackage)
    }

    @Test
    fun toggleOutsideNodesDoesNothing() {
        val ctrl = controller()
        ctrl.start(listOf(node("a", 0, 0, 100, 50)), "com.example.app")
        assertFalse(ctrl.toggleAt(500, 500))
        assertTrue(ctrl.current().isEmpty())
        ctrl.stop()
    }

    @Test
    fun sentenceApplyThenWordClearRemovesOnlyTappedNode() {
        val ctrl = controller()
        ctrl.start(sentenceNodes(), "com.example.app")
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.SENTENCE, MaskModeController.Unit.WORD))
        assertEquals(3, ctrl.current().size)
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.SENTENCE, MaskModeController.Unit.WORD))
        assertEquals(setOf("b", "c"), ctrl.current().map { it.nodeId }.toSet())
        ctrl.stop()
    }

    @Test
    fun sentenceApplyThenSentenceClearRemovesWholeGroup() {
        val ctrl = controller()
        ctrl.start(sentenceNodes(), "com.example.app")
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.SENTENCE, MaskModeController.Unit.SENTENCE))
        assertEquals(3, ctrl.current().size)
        assertTrue(ctrl.toggleAt(150, 25, MaskModeController.Unit.SENTENCE, MaskModeController.Unit.SENTENCE))
        assertTrue(ctrl.current().isEmpty())
        ctrl.stop()
    }

    @Test
    fun wordApplyThenSentenceClearRemovesOnlyThatNode() {
        val ctrl = controller()
        ctrl.start(sentenceNodes(), "com.example.app")
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.WORD, MaskModeController.Unit.SENTENCE))
        assertEquals(1, ctrl.current().size)
        assertEquals("g1", ctrl.current()[0].groupId)
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.WORD, MaskModeController.Unit.SENTENCE))
        assertTrue(ctrl.current().isEmpty())
        ctrl.stop()
    }

    @Test
    fun wordApplyThenSentenceClearRemovesWholeSentenceGroup() {
        val ctrl = controller()
        ctrl.start(sentenceNodes(), "com.example.app")
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.WORD, MaskModeController.Unit.WORD))
        assertTrue(ctrl.toggleAt(150, 25, MaskModeController.Unit.WORD, MaskModeController.Unit.WORD))
        assertEquals(2, ctrl.current().size)
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.WORD, MaskModeController.Unit.SENTENCE))
        assertTrue(ctrl.current().isEmpty())
        ctrl.stop()
    }

    @Test
    fun sentenceApplyAddsOnlyUnmaskedGroupNodes() {
        val ctrl = controller()
        ctrl.start(sentenceNodes(), "com.example.app")
        assertTrue(ctrl.toggleAt(50, 25, MaskModeController.Unit.WORD, MaskModeController.Unit.WORD))
        assertEquals(1, ctrl.current().size)
        assertTrue(ctrl.toggleAt(150, 25, MaskModeController.Unit.SENTENCE, MaskModeController.Unit.WORD))
        assertEquals(setOf("a", "b", "c"), ctrl.current().map { it.nodeId }.toSet())
        ctrl.stop()
    }
}
