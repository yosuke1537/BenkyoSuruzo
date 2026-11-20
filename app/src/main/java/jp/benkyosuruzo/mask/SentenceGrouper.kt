package jp.benkyosuruzo.mask

/**
 * Assigns a sentence-group id to each captured text node. Pure logic so it can
 * be unit-tested without Android framework classes.
 *
 * Grouping strategy (best effort):
 *  1. Nodes inside a "sentence" container (a node whose viewIdResourceName's
 *     simple name is "sentence", e.g. "com.hellochinese:id/sentence") share
 *     that container's unique id.
 *  2. Otherwise, sibling nodes that share the same immediate parent are grouped
 *     when they are vertically close and the preceding node does not end with a
 *     sentence-final punctuation mark.
 */
object SentenceGrouper {

    /** A text node captured from the accessibility tree before grouping. */
    data class RawNode(
        val id: String,
        val text: String,
        val bounds: Bounds,
        val packageName: String,
        val sentenceKey: String?,   // unique sentence-container id, or null
        val parentKey: String       // stable key of the immediate parent
    )

    private val sentenceTerminators = setOf(
        '。', '！', '？',   // Japanese
        '.', '!', '?',     // English
        '；', ';'          // Semicolons (optional)
    )

    fun assign(nodes: List<RawNode>): List<TextNode> {
        val fallbackIds = fallbackGroupIds(nodes.filter { it.sentenceKey == null })
        return nodes.map { node ->
            TextNode(
                id = node.id,
                text = node.text,
                bounds = node.bounds,
                packageName = node.packageName,
                groupId = node.sentenceKey ?: fallbackIds[node.id].orEmpty()
            )
        }
    }

    private fun fallbackGroupIds(nodes: List<RawNode>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        // groupBy preserves encounter order (BFS order from the caller).
        for ((parentKey, siblings) in nodes.groupBy { it.parentKey }) {
            var runIndex = 0
            var prev: RawNode? = null
            for (node in siblings) {
                if (prev != null && !isSameSentenceGroup(prev, node)) runIndex++
                result[node.id] = "$parentKey:$runIndex"
                prev = node
            }
        }
        return result
    }

    private fun isSameSentenceGroup(prev: RawNode, next: RawNode): Boolean {
        val gap = next.bounds.top - prev.bounds.bottom
        if (gap > prev.bounds.height() * 1.5) return false
        return !endsWithTerminator(prev.text)
    }

    private fun endsWithTerminator(text: String): Boolean =
        text.trimEnd().lastOrNull() in sentenceTerminators
}
