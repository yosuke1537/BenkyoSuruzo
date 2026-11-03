package jp.benkyosuruzo.data

import androidx.room.withTransaction
import jp.benkyosuruzo.data.entity.LearningAttributeEntity
import jp.benkyosuruzo.data.entity.LearningEntryEntity
import jp.benkyosuruzo.data.entity.OccurrenceEntity
import jp.benkyosuruzo.text.ScreenText
import java.util.UUID

/**
 * Data-layer facade for learning history. Persists filtered screen text and
 * merges duplicates per (language, normalizedText).
 */
class HistoryRepository(private val db: AppDatabase) {

    suspend fun record(screenText: ScreenText, wasSpoken: Boolean, contextText: String? = null) {
        val language = screenText.language ?: return

        db.withTransaction {
            val entryDao = db.learningEntryDao()
            val occurrenceDao = db.occurrenceDao()
            val now = System.currentTimeMillis()

            screenText.texts.forEach { text ->
                val normalized = TextNormalizer.normalize(text, language)
                if (normalized.isEmpty()) return@forEach

                val entryId = entryDao.getByKey(language, normalized)?.let { existing ->
                    entryDao.updateSeen(existing.id, now, existing.occurrenceCount + 1)
                    existing.id
                } ?: run {
                    val id = UUID.randomUUID().toString()
                    entryDao.insert(
                        LearningEntryEntity(
                            id = id,
                            language = language,
                            normalizedText = normalized,
                            displayText = text,
                            firstSeenAt = now,
                            lastSeenAt = now,
                            occurrenceCount = 1,
                            isFavorite = false,
                            userNote = null,
                            masteryState = "UNLEARNED",
                            tags = "[]",
                            srsNextReviewAt = null,
                            schemaVersion = 1
                        )
                    )
                    id
                }

                occurrenceDao.insert(
                    OccurrenceEntity(
                        id = UUID.randomUUID().toString(),
                        entryId = entryId,
                        seenAt = now,
                        sourcePackage = screenText.packageName,
                        wasSpoken = wasSpoken,
                        contextText = contextText
                    )
                )
            }
        }
    }

    suspend fun getByLanguage(language: String): List<LearningEntryEntity> =
        db.learningEntryDao().getByLanguage(language)

    suspend fun getByKey(language: String, normalizedText: String): LearningEntryEntity? =
        db.learningEntryDao().getByKey(language, normalizedText)

    suspend fun getAll(): List<LearningEntryEntity> =
        db.learningEntryDao().getAll()

    suspend fun deleteEntry(entryId: String) {
        db.learningEntryDao().deleteById(entryId)
    }

    suspend fun deleteAll() {
        db.learningEntryDao().deleteAll()
    }

    suspend fun upsertAttribute(
        entryId: String,
        source: String,
        key: String,
        valueType: String,
        value: String,
        expiresAt: Long?,
        priority: Int
    ) {
        db.learningAttributeDao().upsert(
            LearningAttributeEntity(
                id = UUID.randomUUID().toString(),
                entryId = entryId,
                source = source,
                key = key,
                valueType = valueType,
                value = value,
                fetchedAt = System.currentTimeMillis(),
                expiresAt = expiresAt,
                priority = priority
            )
        )
    }

    suspend fun getAttributes(entryId: String): List<LearningAttributeEntity> =
        db.learningAttributeDao().getByEntry(entryId)
}
