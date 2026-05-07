package com.example.lcb.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val goal: Int,
)

@Entity(tableName = "hydration_records")
data class HydrationRecordEntity(
    @PrimaryKey val id: Long,
    val timestamp: Long,
    val amountMl: Int,
)

@Dao
interface DailyStepDao {
    @Query("SELECT * FROM daily_steps ORDER BY date ASC")
    fun observeAll(): Flow<List<DailyStepEntity>>

    @Query("SELECT * FROM daily_steps ORDER BY date ASC")
    suspend fun getAll(): List<DailyStepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DailyStepEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<DailyStepEntity>)

    @Query(
        """
        DELETE FROM daily_steps
        WHERE date NOT IN (
            SELECT date FROM daily_steps ORDER BY date DESC LIMIT :limit
        )
        """
    )
    suspend fun trimToLatest(limit: Int)
}

@Dao
interface HydrationRecordDao {
    @Query("SELECT * FROM hydration_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<HydrationRecordEntity>>

    @Query("SELECT * FROM hydration_records ORDER BY timestamp DESC")
    suspend fun getAll(): List<HydrationRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HydrationRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HydrationRecordEntity>)

    @Query(
        """
        DELETE FROM hydration_records
        WHERE id NOT IN (
            SELECT id FROM hydration_records ORDER BY timestamp DESC LIMIT :limit
        )
        """
    )
    suspend fun trimToLatest(limit: Int)
}

@Database(
    entities = [
        DailyStepEntity::class,
        HydrationRecordEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class LcbDatabase : RoomDatabase() {
    abstract fun dailyStepDao(): DailyStepDao
    abstract fun hydrationRecordDao(): HydrationRecordDao

    companion object {
        @Volatile
        private var instance: LcbDatabase? = null

        fun getInstance(context: Context): LcbDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LcbDatabase::class.java,
                    "lcb_steps.db",
                ).build().also { instance = it }
            }
        }
    }
}

fun DailyStepEntity.toModel(): StepDailyRecord {
    return StepDailyRecord(date = date, steps = steps, goal = goal)
}

fun StepDailyRecord.toEntity(): DailyStepEntity {
    return DailyStepEntity(date = date, steps = steps, goal = goal)
}

fun HydrationRecordEntity.toModel(): HydrationRecord {
    return HydrationRecord(id = id, timestamp = timestamp, amountMl = amountMl)
}

fun HydrationRecord.toEntity(): HydrationRecordEntity {
    return HydrationRecordEntity(id = id, timestamp = timestamp, amountMl = amountMl)
}
