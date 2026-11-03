package jp.benkyosuruzo.text

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Walks accessibility node trees and collects visible texts.
 *
 * Pure logic, independent from any Service, so it stays unit-testable.
 * Exceptions are intentionally not swallowed here; the calling
 * [jp.benkyosuruzo.service.ScreenTextReaderService] catches and logs them.
 */
class ScreenTextExtractor {

    /**
     * Breadth-first traversal of [roots], gathering trimmed node.text and
     * node.contentDescription values into a duplicate-free, order-preserving
     * list.
     *
     * Bounded by [MAX_NODES] visited nodes and [MAX_DEPTH] levels to protect
     * against infinite trees and expensive screens.
     *
     * Note: nodes are never recycled; AccessibilityNodeInfo.recycle() is a
     * deprecated no-op on API 33+ and calling it can crash the process.
     */
    fun extract(roots: List<AccessibilityNodeInfo>): List<String> {
        val collected = LinkedHashSet<String>()
        val queue = ArrayDeque<NodeAtDepth>()
        roots.forEach { queue.add(NodeAtDepth(it, 0)) }

        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val current = queue.removeFirst()
            if (current.depth > MAX_DEPTH) continue
            visited++

            collect(current.node.text, collected)
            collect(current.node.contentDescription, collected)

            if (current.depth == MAX_DEPTH) continue
            for (i in 0 until current.node.childCount) {
                val child = current.node.getChild(i) ?: continue
                queue.add(NodeAtDepth(child, current.depth + 1))
            }
        }
        return collected.toList()
    }

    private fun collect(raw: CharSequence?, sink: MutableSet<String>) {
        val text = raw?.toString()?.trim().orEmpty()
        if (text.isNotEmpty()) {
            sink.add(text)
        }
    }

    private data class NodeAtDepth(
        val node: AccessibilityNodeInfo,
        val depth: Int
    )

    private companion object {
        const val MAX_NODES = 600
        const val MAX_DEPTH = 30
    }
}
