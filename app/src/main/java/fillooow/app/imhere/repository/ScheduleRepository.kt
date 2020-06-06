package fillooow.app.imhere.repository

import fillooow.app.imhere.db.dao.ScheduleDao
import fillooow.app.imhere.db.entity.ScheduleEntity

class ScheduleRepository(
        private val scheduleDao: ScheduleDao
) {
    suspend fun getSchedule(): List<ScheduleEntity> = scheduleDao.getSchedule()

    suspend fun changPairState(date: String, pairState: String) = scheduleDao.changePairState(date, pairState)
}