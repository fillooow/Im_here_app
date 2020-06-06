package fillooow.app.imhere.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import fillooow.app.imhere.data.VisitState
import fillooow.app.imhere.db.entity.ScheduleEntity

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_table")
    suspend fun getSchedule(): List<ScheduleEntity>

    //изменяет анвизитед на визитед
    @Query("UPDATE schedule_table SET pairState = :pairState  WHERE date = :date")
    suspend fun changePairState(date: String, pairState: String)

    @Insert
    suspend fun insert(pair: ScheduleEntity)
}