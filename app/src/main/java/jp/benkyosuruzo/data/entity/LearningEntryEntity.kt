package jp.benkyosuruzo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Aggregated learning item. Unique per (language, normalizedText).
 */
@Entity(
    tableName = "learning_entry",
    indices = [Index(value = ["language", "normalized_text"], unique = true)]
)
data class LearningEntryEntity(
    @PrimaryKey val id: String,
    val language: String,             // BCP-47 (e.g. "zh-Hans")
    @ColumnInfo(name = "normalized_text") val normalizedText: String,
    @ColumnInfo(name = "display_text") val displayText: String,
    @ColumnInfo(name = "first_seen_at") val firstSeenAt: Long,
    @ColumnInfo(name = "last_seen_at") val lastSeenAt: Long,
    @ColumnInfo(name = "occurrence_count") val occurrenceCount: Int,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "user_note") val userNote: String?,
    @ColumnInfo(name = "mastery_state") val masteryState: String,   // UNLEARNED / LEARNING / MASTERED
    val tags: String,                 // JSON array string (e.g. "[]")
    @ColumnInfo(name = "srs_next_review_at") val srsNextReviewAt: Long?,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int = 1
)
