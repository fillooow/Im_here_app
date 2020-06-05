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
    @Query("UPDATE schedule_table SET visit = :visited  WHERE date = :date")
    suspend fun changeState(date: String, visited: String = VisitState.VISITED.name)

    @Insert
    suspend fun insert(pair: ScheduleEntity)
}