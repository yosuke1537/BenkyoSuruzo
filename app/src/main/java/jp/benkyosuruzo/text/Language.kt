package jp.benkyosuruzo.text

/**
 * Languages the app can target.
 *
 * Each value carries its BCP-47 tag, used later for TTS, storage, and
 * selection logic. [UNKNOWN] represents text whose language could not be
 * determined.
 */
enum class Language(val bcp47: String) {
    CHINESE_SIMPLIFIED("zh-Hans"),
    KOREAN("ko"),
    ENGLISH("en"),
    JAPANESE("ja"),
    UNKNOWN("und")
}
