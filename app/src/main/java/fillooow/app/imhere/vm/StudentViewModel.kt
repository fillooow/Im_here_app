package fillooow.app.imhere.vm

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fillooow.app.imhere.data.filter.CourseType
import fillooow.app.imhere.data.filter.InstitutionType
import fillooow.app.imhere.data.filter.StudentInfo
import fillooow.app.imhere.data.filter.StudentUnionType
import fillooow.app.imhere.db.UrfuRoomDatabase
import fillooow.app.imhere.db.entity.InstitutionEntity
import fillooow.app.imhere.db.entity.ScheduleEntity
import fillooow.app.imhere.repository.InstitutionRepository
import fillooow.app.imhere.repository.InterviewRepository
import fillooow.app.imhere.repository.LocationFacade
import fillooow.app.imhere.repository.ScheduleRepository
import fillooow.app.imhere.utils.STUDENT_INFO_SHARED_PREFS
import com.google.gson.Gson

class StudentViewModel(private val app: Application) : AndroidViewModel(app) {

    private val interviewRepository: InterviewRepository
    private val scheduleRepository: ScheduleRepository
    private val institutionRepository: InstitutionRepository

    fun getLocationFacade(activity: Activity) = LocationFacade(
        activity = activity,
        context = app,
        showToastCallback = { showToast(it) }
    )

    init {

        val interviewDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).interviewDao()

        interviewRepository = InterviewRepository(interviewDao)

        val scheduleDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).scheduleDao()

        scheduleRepository = ScheduleRepository(scheduleDao)

        val institutionDao = UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        ).institutionDao()

        institutionRepository = InstitutionRepository(institutionDao)
    }

    suspend fun getAllInterviews() = interviewRepository.getAllInterviews()

    suspend fun getSchedule(): List<ScheduleEntity> = scheduleRepository.getSchedule()

    suspend fun changePairState(date: String, pairState: String) = scheduleRepository.changPairState(date, pairState)

    fun requestLocationPermission(activity: Activity) = ActivityCompat.requestPermissions(
        activity,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        1
    )

    fun checkLocationPermission() = ActivityCompat.checkSelfPermission(
        app,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    suspend fun getInstitution(prefix: String): InstitutionEntity = institutionRepository.getCoordinates(prefix).first()

    fun loadSavedStudentInfo(): StudentInfo {

        val sp = app.getSharedPreferences(STUDENT_INFO_SHARED_PREFS, Context.MODE_PRIVATE)
        val studentInfoJson = sp.getString(STUDENT_INFO_SHARED_PREFS, "")

        return when (studentInfoJson.isNullOrBlank()) {

            true -> {
                val studentInfo = getFakeStudentInfo()
                saveStudentInfo(studentInfo)
                studentInfo
            }
            false -> Gson().fromJson(studentInfoJson, StudentInfo::class.java)
        }
    }

    private fun saveStudentInfo(studentInfo: StudentInfo) {

        val sp = app.getSharedPreferences(STUDENT_INFO_SHARED_PREFS, Context.MODE_PRIVATE)
        with(sp.edit()) {
            val studentInfoJson = Gson().toJson(studentInfo)
            putString(STUDENT_INFO_SHARED_PREFS, studentInfoJson)
            apply()
        }
    }

    private fun getFakeStudentInfo() = StudentInfo(
        CourseType.FIRST,
        InstitutionType.InFO,
        StudentUnionType.NOT_IN_STUDENT_UNION
    )

    fun showToast(text: String) = Toast.makeText(app, text, Toast.LENGTH_LONG).show()}