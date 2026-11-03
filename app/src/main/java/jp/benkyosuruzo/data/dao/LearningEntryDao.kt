package jp.benkyosuruzo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import jp.benkyosuruzo.data.entity.LearningEntryEntity

@Dao
interface LearningEntryDao {

    @Insert
    suspend fun insert(entry: LearningEntryEntity)

    @Update
    suspend fun update(entry: LearningEntryEntity)

    @Query("SELECT * FROM learning_entry WHERE id = :id")
    suspend fun getById(id: String): LearningEntryEntity?

    @Query("SELECT * FROM learning_entry WHERE language = :language AND normalized_text = :normalizedText LIMIT 1")
    suspend fun getByKey(language: String, normalizedText: String): LearningEntryEntity?

    @Query("SELECT * FROM learning_entry")
    suspend fun getAll(): List<LearningEntryEntity>

    @Query("SELECT * FROM learning_entry WHERE language = :language")
    suspend fun getByLanguage(language: String): List<LearningEntryEntity>

    @Query("UPDATE learning_entry SET last_seen_at = :lastSeenAt, occurrence_count = :occurrenceCount WHERE id = :id")
    suspend fun updateSeen(id: String, lastSeenAt: Long, occurrenceCount: Int)

    @Query("DELETE FROM learning_entry WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM learning_entry")
    suspend fun deleteAll()
}
