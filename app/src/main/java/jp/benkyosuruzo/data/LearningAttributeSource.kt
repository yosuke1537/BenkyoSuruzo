package jp.benkyosuruzo.data

/**
 * Seam for external learning attributes (exam frequency, difficulty,
 * JLPT/HSK levels, etc.).
 *
 * Concrete implementations arrive in TASK-07. Only the interface is defined
 * here so external data can be accepted without schema changes.
 */
interface LearningAttributeSource {
    val sourceId: String

    suspend fun fetch(entryId: String, language: String, text: String): List<AttributeValue>

    data class AttributeValue(
        val key: String,
        val valueType: String,
        val value: String,
        val expiresAt: Long?,
        val priority: Int
    )
}
