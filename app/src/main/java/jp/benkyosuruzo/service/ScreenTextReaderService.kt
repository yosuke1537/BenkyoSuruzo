package jp.benkyosuruzo.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import jp.benkyosuruzo.mask.Bounds
import jp.benkyosuruzo.mask.FloatingButtonController
import jp.benkyosuruzo.mask.MaskModeController
import jp.benkyosuruzo.mask.MaskOverlayService
import jp.benkyosuruzo.mask.TextNode
import jp.benkyosuruzo.settings.SettingsKeys
import jp.benkyosuruzo.ocr.OcrRuntime
import jp.benkyosuruzo.text.FilteringHandler
import jp.benkyosuruzo.text.ScreenText
import jp.benkyosuruzo.text.ScreenTextExtractor
import jp.benkyosuruzo.text.ScreenTextHandler

/**
 * Listens to accessibility events and forwards the text of other apps' screens
 * to a [ScreenTextHandler]. Also triggers OCR fallback when accessibility
 * yields no text (image-rendered screens).
 */
class ScreenTextReaderService : AccessibilityService() {

    private val extractor = ScreenTextExtractor()
    private var handler: ScreenTextHandler? = null
    private var lastHandledUptimeMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        FloatingButtonController.show(this)
        handler = ScreenTextHandler.create(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Never crash on malformed events, empty trees, or missing permission.
        try {
            handleEvent(event)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle accessibility event", e)
        }
    }

    override fun onInterrupt() {
        // Required by AccessibilityService; nothing to interrupt.
    }

    override fun onDestroy() {
        FloatingButtonController.hide()
        instance = null
        handler = null
        OcrRuntime.release()
        // TYPE_ACCESSIBILITY_OVERLAY is auto-removed on disconnect; sync state.
        MaskOverlayService.stop(this)
        super.onDestroy()
    }

    /** Shows or hides the floating mask button based on its setting. */
    fun syncFloatingButton() {
        val enabled = getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(SettingsKeys.MASK_FLOATING_BUTTON_ENABLED, false)
        if (enabled) FloatingButtonController.show(this)
        else FloatingButtonController.hide()
    }

    private fun handleEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType !in HANDLED_EVENT_TYPES) return
        // Ignore our own window to prevent feedback loops.
        if (event.packageName?.toString() == packageName) return

        // Auto-stop mask mode when the foreground app changes (safety measure).
        maybeAutoStopMaskMode(event)

        // Simple 500ms throttle on top of notificationTimeout in the XML.
        val now = SystemClock.uptimeMillis()
        if (now - lastHandledUptimeMs < THROTTLE_MS) return

        val roots = collectRoots(event)
        if (roots.isEmpty()) return
        lastHandledUptimeMs = now

        val pkg = event.packageName?.toString().orEmpty()
        val texts = extractor.extract(roots)
        val target = handler

        var kept = 0
        if (target != null && texts.isNotEmpty()) {
            target.onScreenText(
                ScreenText(
                    packageName = pkg,
                    texts = texts,
                    timestampMs = System.currentTimeMillis()
                )
            )
            kept = (target as? FilteringHandler)?.lastOutputCount ?: 0
        }

        // OCR fallback for image-rendered screens where accessibility yields nothing.
        OcrRuntime.maybeRunOcr(pkg, kept, isSensitive = currentTreeHasPassword(roots))
        OcrRuntime.notifyWindowChanged()
    }

    /**
     * Root nodes in priority order:
     * 1. every window root (needs FLAG_RETRIEVE_INTERACTIVE_WINDOWS)
     * 2. the active window root
     * 3. the event source node
     * 4. nothing (empty list) when all are unavailable
     */
    private fun collectRoots(event: AccessibilityEvent): List<AccessibilityNodeInfo> {
        val windowRoots = windows?.mapNotNull { it.root }.orEmpty()
        if (windowRoots.isNotEmpty()) return windowRoots

        rootInActiveWindow?.let { return listOf(it) }
        event.source?.let { return listOf(it) }
        return emptyList()
    }

    /**
     * Collects all maskable text nodes currently on screen for mask mode.
     * Bounded BFS; nodes are not recycled here.
     */
    fun snapshotTextNodes(): List<TextNode> {
        // Use the active window (the foreground app) rather than all
        // interactive windows, so system UI (status bar / notification shade)
        // does not leak into the mask targets.
        val root = rootInActiveWindow ?: return emptyList()

        val nodes = mutableListOf<TextNode>()
        val queue = ArrayDeque<NodeDepth>()
        queue.add(NodeDepth(root, 0))
        val bounds = Rect()
        var index = 0

        while (queue.isNotEmpty() && nodes.size < MAX_SNAPSHOT_NODES) {
            val current = queue.removeFirst()
            if (current.depth > MAX_SNAPSHOT_DEPTH) continue

            val text = current.node.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty() && current.node.isVisibleToUser) {
                current.node.getBoundsInScreen(bounds)
                if (!bounds.isEmpty) {
                    nodes.add(
                        TextNode(
                            id = "${bounds.left},${bounds.top},${bounds.right},${bounds.bottom}:$text:$index",
                            text = text,
                            bounds = Bounds(bounds.left, bounds.top, bounds.right, bounds.bottom),
                            packageName = current.node.packageName?.toString().orEmpty()
                        )
                    )
                    index++
                }
            }

            if (current.depth == MAX_SNAPSHOT_DEPTH) continue
            for (i in 0 until current.node.childCount) {
                val child = current.node.getChild(i) ?: continue
                queue.add(NodeDepth(child, current.depth + 1))
            }
        }
        return nodes
    }

    /**
     * Stops mask mode when the foreground app changes away from the package
     * that was active when the mode started (safety measure).
     */
    private fun maybeAutoStopMaskMode(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        val controller = MaskModeController.get()
        if (controller.active &&
            pkg != controller.originPackage &&
            pkg != "com.android.systemui" &&
            pkg != packageName
        ) {
            MaskOverlayService.stop(this)
        }
    }

    /**
     * Best-effort sensitive-screen detection: scans the accessibility tree for
     * password fields. Bounded BFS so it cannot loop on huge trees.
     */
    private fun currentTreeHasPassword(roots: List<AccessibilityNodeInfo>): Boolean {
        var visited = 0
        val queue = ArrayDeque<NodeDepth>()
        roots.forEach { queue.add(NodeDepth(it, 0)) }

        while (queue.isNotEmpty() && visited < MAX_PASSWORD_SCAN_NODES) {
            val current = queue.removeFirst()
            if (current.depth > MAX_PASSWORD_SCAN_DEPTH) continue
            visited++
            if (current.node.isPassword) return true
            if (current.depth == MAX_PASSWORD_SCAN_DEPTH) continue

            for (i in 0 until current.node.childCount) {
                val child = current.node.getChild(i) ?: continue
                queue.add(NodeDepth(child, current.depth + 1))
            }
        }
        return false
    }

    private data class NodeDepth(val node: AccessibilityNodeInfo, val depth: Int)

    companion object {
        const val TAG = "ScreenTextReader"
        const val THROTTLE_MS = 500L
        const val MAX_PASSWORD_SCAN_NODES = 200
        const val MAX_PASSWORD_SCAN_DEPTH = 20
        const val MAX_SNAPSHOT_NODES = 600
        const val MAX_SNAPSHOT_DEPTH = 30

        @Volatile
        private var instance: ScreenTextReaderService? = null

        /** Current service instance, or null when the service is not connected. */
        fun current(): ScreenTextReaderService? = instance

        val HANDLED_EVENT_TYPES = setOf(
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED
        )
    }
}

