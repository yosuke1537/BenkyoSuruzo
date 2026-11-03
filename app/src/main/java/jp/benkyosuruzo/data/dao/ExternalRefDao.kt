package jp.benkyosuruzo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import jp.benkyosuruzo.data.entity.ExternalRefEntity

@Dao
interface ExternalRefDao {

    @Insert
    suspend fun insert(r: ExternalRefEntity)

    @Query("SELECT * FROM external_ref WHERE entry_id = :entryId")
    suspend fun getByEntry(entryId: String): List<ExternalRefEntity>

    @Query("DELETE FROM external_ref WHERE entry_id = :entryId")
    suspend fun deleteByEntry(entryId: String)
}
