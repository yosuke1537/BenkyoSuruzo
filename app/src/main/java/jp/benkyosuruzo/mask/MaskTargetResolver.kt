package jp.benkyosuruzo.mask

/**
 * Maps a tap in screen coordinates to the most specific [TextNode] under it.
 * Pure logic so it can be unit-tested without Android framework classes.
 */
class MaskTargetResolver(private val nodes: List<TextNode>) {

    /** Returns the smallest node whose bounds contain the point, or null. */
    fun findTargetAt(x: Int, y: Int): TextNode? =
        nodes.filter { it.bounds.contains(x, y) }
            .minByOrNull { it.bounds.width().toLong() * it.bounds.height() }

    fun all(): List<TextNode> = nodes
}
