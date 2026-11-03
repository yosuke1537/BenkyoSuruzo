package jp.benkyosuruzo.text

/**
 * Seam for the currently selected language.
 *
 * Replaced in TASK-04 with a SharedPreferences-backed implementation so the
 * user can change the language at runtime.
 */
fun interface LanguageProvider {
    fun current(): Language
}

/** TASK-02 default implementation, fixed to a single language. Replaced in TASK-04. */
class FixedLanguageProvider(
    private val language: Language = Language.CHINESE_SIMPLIFIED
) : LanguageProvider {
    override fun current(): Language = language
}
