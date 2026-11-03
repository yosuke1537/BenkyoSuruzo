package jp.benkyosuruzo.ocr

/**
 * Decides whether OCR should run for a given screen, based on settings and
 * throttle state. Pure logic, testable in isolation.
 */
class OcrTriggerPolicy(
    private val settings: OcrSettingsProvider,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private var lastOcrAt = 0L
    private var lastWindowChangedAt = 0L

    fun shouldRun(packageName: String, accessibilityKeptCount: Int, isSensitive: Boolean): Boolean {
        if (!settings.isOcrEnabled()) return false
        if (packageName !in settings.allowedPackages()) return false
        // Only run OCR when accessibility captured nothing.
        if (accessibilityKeptCount != 0) return false
        if (isSensitive) return false

        val now = clock()
        if (now - lastOcrAt < COOLDOWN_MS) return false
        if (now - lastWindowChangedAt < WINDOW_SETTLE_MS) return false

        lastOcrAt = now
        return true
    }

    fun notifyWindowChanged() {
        lastWindowChangedAt = clock()
    }

    private companion object {
        const val COOLDOWN_MS = 3000L
        const val WINDOW_SETTLE_MS = 500L
    }
}
