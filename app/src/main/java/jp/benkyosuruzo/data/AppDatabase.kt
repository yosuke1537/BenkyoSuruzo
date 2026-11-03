package jp.benkyosuruzo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import jp.benkyosuruzo.data.dao.ExternalRefDao
import jp.benkyosuruzo.data.dao.LearningAttributeDao
import jp.benkyosuruzo.data.dao.LearningEntryDao
import jp.benkyosuruzo.data.dao.OccurrenceDao
import jp.benkyosuruzo.data.entity.ExternalRefEntity
import jp.benkyosuruzo.data.entity.LearningAttributeEntity
import jp.benkyosuruzo.data.entity.LearningEntryEntity
import jp.benkyosuruzo.data.entity.OccurrenceEntity

@Database(
    entities = [
        LearningEntryEntity::class,
        OccurrenceEntity::class,
        LearningAttributeEntity::class,
        ExternalRefEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun learningEntryDao(): LearningEntryDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun learningAttributeDao(): LearningAttributeDao
    abstract fun externalRefDao(): ExternalRefDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "benkyo_suruzo.db"
                ).build().also { instance = it }
            }
    }
}
