package jp.benkyosuruzo.text

/**
 * Snapshot of the text visible on a foreign app's screen.
 *
 * @property packageName package that owns the window the text came from.
 * @property texts screen texts in on-screen order, duplicates already removed.
 * @property timestampMs wall-clock time when the snapshot was taken.
 * @property language BCP-47 language tag; set once the text passes the filter.
 */
data class ScreenText(
    val packageName: String,
    val texts: List<String>,
    val timestampMs: Long,
    val language: String? = null
)

