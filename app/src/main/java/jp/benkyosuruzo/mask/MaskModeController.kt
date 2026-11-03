package jp.benkyosuruzo.mask

import android.util.Log

/**
 * Holds the active mask session. Every method is synchronized because the
 * accessibility callback (auto-stop) and the overlay view (touches) run on
 * different threads.
 */
class MaskModeController private constructor() {

    private var resolver: MaskTargetResolver = MaskTargetResolver(emptyList())
    private val masks = mutableListOf<MaskRect>()

    @Volatile
    var active: Boolean = false
        private set

    @Volatile
    var originPackage: String? = null
        private set

    @Synchronized
    fun start(textNodes: List<TextNode>, originPackage: String) {
        resolver = MaskTargetResolver(textNodes)
        masks.clear()
        this.originPackage = originPackage
        active = true
    }

    @Synchronized
    fun stop() {
        resolver = MaskTargetResolver(emptyList())
        masks.clear()
        originPackage = null
        active = false
    }

    /**
     * Toggles the mask at the given screen coordinates. Returns true when the
     * mask set changed, false otherwise.
     */
    @Synchronized
    fun toggleAt(x: Int, y: Int): Boolean {
        val existing = masks.filter { it.bounds.contains(x, y) }
            .minByOrNull { it.bounds.width().toLong() * it.bounds.height() }
        if (existing != null) {
            masks.remove(existing)
            Log.d(TAG, "mask removed id=${existing.nodeId}")
            return true
        }
        val node = resolver.findTargetAt(x, y) ?: return false
        if (masks.any { it.nodeId == node.id }) return false
        masks.add(
            MaskRect(
                nodeId = node.id,
                text = node.text,
                bounds = Bounds(node.bounds.left, node.bounds.top, node.bounds.right, node.bounds.bottom)
            )
        )
        Log.d(TAG, "mask added id=${node.id}")
        return true
    }

    /** Returns a defensive copy of the current masks. */
    @Synchronized
    fun current(): List<MaskRect> = masks.toList()

    companion object {
        private const val TAG = "MaskModeController"

        @Volatile
        private var inst: MaskModeController? = null

        fun get(): MaskModeController =
            inst ?: synchronized(this) {
                inst ?: MaskModeController().also { inst = it }
            }
    }
}

/** A mask placed over a single text node. */
data class MaskRect(
    val nodeId: String,
    val text: String,
    val bounds: Bounds
)
