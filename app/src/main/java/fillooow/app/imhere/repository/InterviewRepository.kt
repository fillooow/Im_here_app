package fillooow.app.imhere.repository

import fillooow.app.imhere.db.dao.InterviewDao
import fillooow.app.imhere.db.entity.InterviewEntity

class InterviewRepository(
    private val interviewDao: InterviewDao
) {

    suspend fun insertInterview(interview: InterviewEntity) = interviewDao.insert(interview)

    suspend fun getAllInterviews(): List<InterviewEntity> = interviewDao.getAllInterviews()
}