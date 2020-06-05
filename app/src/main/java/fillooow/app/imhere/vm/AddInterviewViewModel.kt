package fillooow.app.imhere.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fillooow.app.imhere.db.UrfuRoomDatabase
import fillooow.app.imhere.db.entity.InterviewEntity
import fillooow.app.imhere.repository.InterviewRepository

class AddInterviewViewModel(private val app: Application) : AndroidViewModel(app) {

    private val interviewRepository: InterviewRepository

    init {

        val interviewDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).interviewDao()

        interviewRepository = InterviewRepository(interviewDao)
    }

    suspend fun insertInterview(interview: InterviewEntity) = interviewRepository.insertInterview(interview)
}