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
     * Toggles the mask at the given screen coordinates using word-level units
     * (backward compatible with TASK-09). Returns true when the mask set
     * changed, false otherwise.
     */
    @Synchronized
    fun toggleAt(x: Int, y: Int): Boolean = toggleAt(x, y, Unit.WORD, Unit.WORD)

    /**
     * Toggles the mask at the given screen coordinates.
     *
     * @param applyUnit unit to use when adding a mask (WORD / SENTENCE)
     * @param clearUnit unit to use when removing a mask (WORD / SENTENCE)
     * @return true when the mask set changed, false otherwise
     */
    @Synchronized
    fun toggleAt(x: Int, y: Int, applyUnit: Unit, clearUnit: Unit): Boolean {
        val existing = masks.filter { it.bounds.contains(x, y) }
            .minByOrNull { it.bounds.width().toLong() * it.bounds.height() }
        if (existing != null) {
            return when (clearUnit) {
                Unit.WORD -> {
                    val removed = masks.remove(existing)
                    Log.d(TAG, "clear word id=${existing.nodeId}")
                    removed
                }
                Unit.SENTENCE -> {
                    val removed = masks.removeAll { it.groupId == existing.groupId }
                    Log.d(TAG, "clear sentence group=${existing.groupId} removed=$removed")
                    removed
                }
            }
        }
        val node = resolver.findTargetAt(x, y) ?: return false
        return when (applyUnit) {
            Unit.WORD -> {
                if (masks.any { it.nodeId == node.id }) return false
                masks.add(
                    MaskRect(
                        nodeId = node.id,
                        text = node.text,
                        bounds = Bounds(node.bounds.left, node.bounds.top, node.bounds.right, node.bounds.bottom),
                        groupId = node.groupId
                    )
                )
                Log.d(TAG, "mask added id=${node.id}")
                true
            }
            Unit.SENTENCE -> {
                val groupNodes = resolver.all().filter { it.groupId == node.groupId }
                if (groupNodes.isEmpty()) return false
                if (groupNodes.all { g -> masks.any { it.nodeId == g.id } }) return false
                var added = 0
                groupNodes.forEach { g ->
                    if (masks.none { it.nodeId == g.id }) {
                        masks.add(
                            MaskRect(
                                nodeId = g.id,
                                text = g.text,
                                bounds = Bounds(g.bounds.left, g.bounds.top, g.bounds.right, g.bounds.bottom),
                                groupId = node.groupId
                            )
                        )
                        added++
                    }
                }
                Log.d(TAG, "apply sentence group=${node.groupId} nodes=$added")
                true
            }
        }
    }

    /** Returns a defensive copy of the current masks. */
    @Synchronized
    fun current(): List<MaskRect> = masks.toList()

    /** Masking/clearing granularity. */
    enum class Unit { WORD, SENTENCE }

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
    val bounds: Bounds,
    val groupId: String
)
