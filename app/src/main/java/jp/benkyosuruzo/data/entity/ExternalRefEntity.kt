package jp.benkyosuruzo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Loose coupling between a learning entry and an external dictionary/data
 * source, tracked by external id.
 */
@Entity(
    tableName = "external_ref",
    foreignKeys = [ForeignKey(
        entity = LearningEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("entry_id"),
        Index(value = ["external_source", "external_id"])
    ]
)
data class ExternalRefEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "entry_id") val entryId: String,
    @ColumnInfo(name = "external_source") val externalSource: String,
    @ColumnInfo(name = "external_id") val externalId: String,
    @ColumnInfo(name = "linked_at") val linkedAt: Long
)
