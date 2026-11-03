package jp.benkyosuruzo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One capture of a learning entry, kept in chronological order.
 */
@Entity(
    tableName = "occurrence",
    foreignKeys = [ForeignKey(
        entity = LearningEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("entry_id"), Index("seen_at")]
)
data class OccurrenceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "entry_id") val entryId: String,
    @ColumnInfo(name = "seen_at") val seenAt: Long,
    @ColumnInfo(name = "source_package") val sourcePackage: String,
    @ColumnInfo(name = "was_spoken") val wasSpoken: Boolean,
    @ColumnInfo(name = "context_text") val contextText: String?
)
