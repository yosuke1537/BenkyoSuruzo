package jp.benkyosuruzo.settings

import android.content.Context
import jp.benkyosuruzo.ocr.CaptureMode
import jp.benkyosuruzo.ocr.OcrSettingsProvider

/**
 * Production [OcrSettingsProvider] backed by SharedPreferences.
 *
 * All reads fall back to a safe default when a value is missing or has the
 * wrong type, so the app never crashes on a fresh install or a corrupt value.
 */
class SharedPreferencesOcrSettingsProvider(context: Context) : OcrSettingsProvider {

    private val prefs = context.getSharedPreferences(SettingsKeys.PREFS_NAME, Context.MODE_PRIVATE)

    override fun isOcrEnabled(): Boolean =
        prefs.getBoolean(SettingsKeys.OCR_ENABLED, false)

    override fun captureMode(): CaptureMode =
        captureModeFrom(prefs.getString(SettingsKeys.CAPTURE_MODE, "once"))

    override fun allowedPackages(): Set<String> =
        prefs.getStringSet(SettingsKeys.ALLOWED_PACKAGES, emptySet())?.toSet() ?: emptySet()

    override fun adoptionScoreThreshold(): Int =
        prefs.getInt(SettingsKeys.ADOPTION_THRESHOLD, 50).coerceIn(0, 100)

    override fun selectedLanguageBcp47(): String =
        prefs.getString(SettingsKeys.SELECTED_LANGUAGE, "zh-Hans") ?: "zh-Hans"
}

/** Pure mapping from the stored capture-mode string to [CaptureMode]; defaults to ONCE. */
internal fun captureModeFrom(value: String?): CaptureMode =
    when (value) {
        "persistent" -> CaptureMode.PERSISTENT
        else -> CaptureMode.ONCE
    }
