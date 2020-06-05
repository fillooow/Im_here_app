package fillooow.app.imhere.repository

import fillooow.app.imhere.db.dao.ScheduleDao
import fillooow.app.imhere.db.entity.ScheduleEntity

class ScheduleRepository(
        private val scheduleDao: ScheduleDao
) {
    suspend fun getSchedule(): List<ScheduleEntity> = scheduleDao.getSchedule()

    suspend fun changeState(date: String) = scheduleDao.changeState(date)
}