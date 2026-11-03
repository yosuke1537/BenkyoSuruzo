package jp.benkyosuruzo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Extensible learning attribute (exam frequency, difficulty, JLPT/HSK level,
 * etc.). New attributes can be added without schema changes.
 *
 * Uniqueness is (entryId, source, key); conflicts resolve via priority and
 * fetchedAt, and expiresAt provides a TTL for re-fetching.
 */
@Entity(
    tableName = "learning_attribute",
    foreignKeys = [ForeignKey(
        entity = LearningEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("entry_id"),
        Index(value = ["entry_id", "source", "key"], unique = true)
    ]
)
data class LearningAttributeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "entry_id") val entryId: String,
    val source: String,               // "hsk", "jlpt", "custom-dict", "user", ...
    val key: String,                  // "exam_frequency", "difficulty", "jlpt_level", ...
    @ColumnInfo(name = "value_type") val valueType: String,   // NUMBER / STRING / BOOLEAN / JSON
    val value: String,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long,
    @ColumnInfo(name = "expires_at") val expiresAt: Long?,
    val priority: Int = 0
)
