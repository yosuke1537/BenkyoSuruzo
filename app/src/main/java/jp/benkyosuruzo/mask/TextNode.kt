package jp.benkyosuruzo.mask

/**
 * A screen-coordinate rectangle. Kept as a plain data class instead of
 * android.graphics.Rect so mask logic can be unit-tested on the JVM.
 */
data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val isEmpty: Boolean get() = left >= right || top >= bottom

    fun width(): Int = right - left

    fun height(): Int = bottom - top

    fun contains(x: Int, y: Int): Boolean =
        x in left until right && y in top until bottom
}

/**
 * A single maskable text node captured from the accessibility tree.
 */
data class TextNode(
    val id: String,
    val text: String,
    val bounds: Bounds,
    val packageName: String,
    val groupId: String
)
