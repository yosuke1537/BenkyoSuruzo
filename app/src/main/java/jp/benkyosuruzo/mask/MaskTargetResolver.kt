package jp.benkyosuruzo.mask

import jp.benkyosuruzo.text.LanguageDetector

/**
 * Maps a tap in screen coordinates to the most specific [TextNode] under it.
 * Pure logic so it can be unit-tested without Android framework classes.
 */
class MaskTargetResolver(
    private val nodes: List<TextNode>,
    private val excludedLanguages: Set<String> = emptySet(),
    private val detector: LanguageDetector = LanguageDetector()
) {

    /** Returns the smallest maskable node whose bounds contain the point, or null. */
    fun findTargetAt(x: Int, y: Int): TextNode? =
        nodes.filter { it.bounds.contains(x, y) && isMaskable(it.text) }
            .minByOrNull { it.bounds.width().toLong() * it.bounds.height() }

    fun all(): List<TextNode> = nodes.filter { isMaskable(it.text) }

    /** Excludes symbol-only, empty, and excluded-language strings. */
    private fun isMaskable(text: String): Boolean {
        val t = text.trim()
        if (t.isEmpty()) return false
        // Symbol-only nodes (no letter or digit) are never masked.
        if (t.none { Character.isLetterOrDigit(it) }) return false
        // Nodes detected as an excluded language are not masked.
        if (excludedLanguages.isNotEmpty()) {
            val code = detector.detect(t).bcp47.substringBefore("-")
            if (code in excludedLanguages) return false
        }
        return true
    }
}
