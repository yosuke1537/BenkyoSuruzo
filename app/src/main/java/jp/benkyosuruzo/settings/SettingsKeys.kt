package jp.benkyosuruzo.settings

/**
 * SharedPreferences keys for all app settings. Values are read by
 * [SharedPreferencesOcrSettingsProvider] and, from TASK-03 onward, by the
 * TextToSpeech layer.
 */
object SettingsKeys {
    // OCR
    const val OCR_ENABLED = "ocr_enabled"
    const val CAPTURE_MODE = "capture_mode"            // "once" | "persistent"
    const val ALLOWED_PACKAGES = "allowed_packages"    // StringSet
    const val ADOPTION_THRESHOLD = "adoption_threshold" // Int 0..100
    const val SELECTED_LANGUAGE = "selected_language"  // BCP-47

    // Speech (used by TASK-03)
    const val SPEECH_ENABLED = "speech_enabled"
    const val SPEECH_RATE = "speech_rate"              // Float 0.5..2.0

    // Mask overlay (TASK-09)
    const val MASK_FEATURE_ENABLED = "mask_feature_enabled"   // Boolean, default false

    const val PREFS_NAME = "benkyo_settings"
}
