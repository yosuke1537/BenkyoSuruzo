package jp.benkyosuruzo.data

import java.text.Normalizer

/**
 * Normalizes captured text before storage so equivalent strings collapse to
 * the same history entry. Returns an empty string when nothing remains; the
 * caller is responsible for skipping empty results.
 */
object TextNormalizer {

    fun normalize(text: String, language: String): String {
        var result = text.trim()
        result = Normalizer.normalize(result, Normalizer.Form.NFKC)
        result = result.replace(Regex("\\s+"), " ")
        if (language.startsWith("en")) {
            result = result.lowercase()
        }
        return result
    }
}
