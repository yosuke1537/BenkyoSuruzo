package jp.benkyosuruzo.ocr

/**
 * Seam for OCR settings. Replaced with a SharedPreferences-backed implementation
 * in TASK-04.
 */
interface OcrSettingsProvider {
    fun isOcrEnabled(): Boolean
    fun captureMode(): CaptureMode
    fun allowedPackages(): Set<String>
    fun adoptionScoreThreshold(): Int   // 0..100
    fun selectedLanguageBcp47(): String
}

enum class CaptureMode { ONCE, PERSISTENT }
