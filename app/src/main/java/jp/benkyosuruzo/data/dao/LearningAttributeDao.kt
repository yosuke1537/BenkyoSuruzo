package jp.benkyosuruzo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.benkyosuruzo.data.entity.LearningAttributeEntity

@Dao
interface LearningAttributeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(a: LearningAttributeEntity)

    @Query("SELECT * FROM learning_attribute WHERE entry_id = :entryId")
    suspend fun getByEntry(entryId: String): List<LearningAttributeEntity>

    @Query("DELETE FROM learning_attribute WHERE entry_id = :entryId")
    suspend fun deleteByEntry(entryId: String)

    @Query("DELETE FROM learning_attribute WHERE expires_at IS NOT NULL AND expires_at < :now")
    suspend fun deleteExpired(now: Long)
}
