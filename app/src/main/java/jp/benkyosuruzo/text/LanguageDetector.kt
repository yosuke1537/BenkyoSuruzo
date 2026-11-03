package jp.benkyosuruzo.text

/**
 * Pure, stateless language detection over a single string.
 *
 * Rules are evaluated top to bottom and the first match wins:
 *
 * 1. Hiragana or Katakana -> [Language.JAPANESE]
 * 2. Hangul -> [Language.KOREAN]
 * 3. CJK Unified Ideographs -> [Language.CHINESE_SIMPLIFIED]
 * 4. Latin letters (including full-width) -> [Language.ENGLISH]
 * 5. Anything else (digits, symbols, whitespace, empty) -> [Language.UNKNOWN]
 *
 * Detection is intentionally not guaranteed to be exact. For example, a
 * short kanji-only word is classified as Chinese, which is an accepted
 * limitation.
 */
class LanguageDetector {

    fun detect(text: String): Language {
        if (text.any { it.isHiragana() || it.isKatakana() }) return Language.JAPANESE
        if (text.any { it.isHangul() }) return Language.KOREAN
        if (text.any { it.isCjkUnifiedIdeograph() }) return Language.CHINESE_SIMPLIFIED
        if (text.any { it.isLatinLetter() }) return Language.ENGLISH
        return Language.UNKNOWN
    }
}

private fun Char.isHiragana(): Boolean = this in '\u3040'..'\u309F'
private fun Char.isKatakana(): Boolean = this in '\u30A0'..'\u30FF'

private fun Char.isHangul(): Boolean =
    this in '\uAC00'..'\uD7AF' ||
        this in '\u1100'..'\u11FF' ||
        this in '\u3130'..'\u318F'

private fun Char.isCjkUnifiedIdeograph(): Boolean = this in '\u4E00'..'\u9FFF'

private fun Char.isLatinLetter(): Boolean =
    this in '\u0041'..'\u005A' ||
        this in '\u0061'..'\u007A' ||
        this in '\uFF21'..'\uFF3A' ||
        this in '\uFF41'..'\uFF5A'
