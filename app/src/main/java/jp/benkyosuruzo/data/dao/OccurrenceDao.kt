package jp.benkyosuruzo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import jp.benkyosuruzo.data.entity.OccurrenceEntity

@Dao
interface OccurrenceDao {

    @Insert
    suspend fun insert(o: OccurrenceEntity)

    @Query("SELECT * FROM occurrence WHERE entry_id = :entryId")
    suspend fun getByEntry(entryId: String): List<OccurrenceEntity>

    @Query("DELETE FROM occurrence WHERE entry_id = :entryId")
    suspend fun deleteByEntry(entryId: String)

    @Query("DELETE FROM occurrence")
    suspend fun deleteAll()
}
