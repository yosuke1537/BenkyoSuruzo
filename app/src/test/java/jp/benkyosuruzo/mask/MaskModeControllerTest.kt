package jp.benkyosuruzo.mask

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaskModeControllerTest {

    private fun controller() = MaskModeController.get()

    private fun node(id: String, l: Int, t: Int, r: Int, b: Int) =
        TextNode(id, "text-$id", Bounds(l, t, r, b), "com.example.app")

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
}
