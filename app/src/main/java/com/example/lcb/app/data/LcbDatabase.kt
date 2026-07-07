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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Entity(tableName = "weight_records")
data class WeightRecordEntity(
    @PrimaryKey val date: String,
    val weightTenthsKg: Int,
    val timestamp: Long,
)

@Entity(
    tableName = "hourly_steps",
    primaryKeys = ["date", "hour"],
)
data class HourlyStepEntity(
    val date: String,
    val hour: Int,
    val steps: Int,
)

@Dao
interface DailyStepDao {
    @Query("SELECT * FROM daily_steps ORDER BY date ASC")
    fun observeAll(): Flow<List<DailyStepEntity>>

    @Query("SELECT * FROM daily_steps ORDER BY date ASC")
    suspend fun getAll(): List<DailyStepEntity>

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailyStepEntity?

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

@Dao
interface WeightRecordDao {
    @Query("SELECT * FROM weight_records ORDER BY date DESC")
    fun observeAll(): Flow<List<WeightRecordEntity>>

    @Query("SELECT * FROM weight_records WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): WeightRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: WeightRecordEntity)

    @Query(
        """
        DELETE FROM weight_records
        WHERE date NOT IN (
            SELECT date FROM weight_records ORDER BY date DESC LIMIT :limit
        )
        """
    )
    suspend fun trimToLatest(limit: Int)
}

@Dao
interface HourlyStepDao {
    @Query("SELECT * FROM hourly_steps ORDER BY date ASC, hour ASC")
    fun observeAll(): Flow<List<HourlyStepEntity>>

    @Query("SELECT * FROM hourly_steps WHERE date = :date AND hour = :hour LIMIT 1")
    suspend fun getByDateHour(date: String, hour: Int): HourlyStepEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: HourlyStepEntity)

    @Query(
        """
        DELETE FROM hourly_steps
        WHERE date NOT IN (
            SELECT DISTINCT date FROM hourly_steps ORDER BY date DESC LIMIT :limit
        )
        """
    )
    suspend fun trimToLatestDays(limit: Int)
}

@Database(
    entities = [
        DailyStepEntity::class,
        HydrationRecordEntity::class,
        WeightRecordEntity::class,
        HourlyStepEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class LcbDatabase : RoomDatabase() {
    abstract fun dailyStepDao(): DailyStepDao
    abstract fun hydrationRecordDao(): HydrationRecordDao
    abstract fun weightRecordDao(): WeightRecordDao
    abstract fun hourlyStepDao(): HourlyStepDao

    companion object {
        @Volatile
        private var instance: LcbDatabase? = null

        private val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weight_records` (
                        `date` TEXT NOT NULL,
                        `weightTenthsKg` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        PRIMARY KEY(`date`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val Migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `hourly_steps` (
                        `date` TEXT NOT NULL,
                        `hour` INTEGER NOT NULL,
                        `steps` INTEGER NOT NULL,
                        PRIMARY KEY(`date`, `hour`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): LcbDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LcbDatabase::class.java,
                    "lcb_steps.db",
                )
                    .addMigrations(Migration1To2, Migration2To3)
                    .build()
                    .also { instance = it }
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

fun WeightRecordEntity.toModel(): WeightRecord {
    return WeightRecord(date = date, weightTenthsKg = weightTenthsKg, timestamp = timestamp)
}

fun WeightRecord.toEntity(): WeightRecordEntity {
    return WeightRecordEntity(date = date, weightTenthsKg = weightTenthsKg, timestamp = timestamp)
}

fun HourlyStepEntity.toBucket(): TrendBucket {
    return TrendBucket(hour = hour.coerceIn(0, 23), steps = steps.coerceAtLeast(0).toLong())
}
