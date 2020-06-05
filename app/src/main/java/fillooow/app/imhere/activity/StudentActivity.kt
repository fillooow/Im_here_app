package fillooow.app.imhere.activity

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.TabHost.TabSpec
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fillooow.app.imhere.R
import fillooow.app.imhere.adapter.InterviewRecyclerViewAdapter
import fillooow.app.imhere.adapter.ScheduleRecyclerViewAdapter
import fillooow.app.imhere.data.VisitState
import fillooow.app.imhere.data.filter.StudentInfo
import fillooow.app.imhere.db.entity.InterviewEntity
import fillooow.app.imhere.utils.AUTHENTICATION_SHARED_PREFS
import fillooow.app.imhere.utils.GpsSwitchBroadcastReceiver
import fillooow.app.imhere.vm.StudentViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationRequest
import kotlinx.android.synthetic.main.student_main.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.GregorianCalendar

private const val MONTH = 0
private const val DAY = 1
private const val YEAR = 2
private const val HOURS = 3
private const val MINUTES = 4

private const val ALL_PERMISSIONS_RESULT = 1011
private const val PLAY_SERVICES_RESOLUTION_REQUEST = 1234

class StudentActivity : AppCompatActivity() {

    private val gpsBroadcastReceiver = GpsSwitchBroadcastReceiver(
        onGPSEnabledAction = { locationFacade.tryToConnectGPSServices() },
        onGPSDisabledAction = { studentViewModel.showToast("без GPS приложение может работать неверно") }
    )

    private val permissions: MutableList<String> = mutableListOf()
    private val permissionsRejected: MutableList<String> = mutableListOf()

    private val locationFacade by lazy {
        studentViewModel.getLocationFacade(this)
    }

    private lateinit var studentViewModel: StudentViewModel

    private val currentDate by lazy { GregorianCalendar() }

    //TODO: разнести
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.student_main)

        studentViewModel = ViewModelProvider(this).get(StudentViewModel::class.java)

        location_progress_bar.visibility = View.INVISIBLE

        permissions.add(ACCESS_FINE_LOCATION)
        permissions.add(ACCESS_COARSE_LOCATION)

        if (permissions.size > 0) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), ALL_PERMISSIONS_RESULT)
        }

        classCardCreate()
        tabHostCreate()

        listViewCreate(studentInfo = studentViewModel.loadSavedStudentInfo())
    }

    // todo: разнести логику
    fun onCheckBtnClick(v: View) {
        studentViewModel.viewModelScope.launch {

            val schedule = studentViewModel.getSchedule()
            val nextPairs = schedule.filter {
                it.date.toGregorianCalendar() > currentDate
                        && isItCurrentDay(getSplitForStringDate(it.date))
                        && it.type != "Онлайн-курс"
            }//Оставшиеся пары на день

            val currentPair = when (nextPairs.isEmpty()) {

                true -> null
                false -> nextPairs.first()
            }

            val toastText = when {

                currentPair == null -> "На сегодня пар больше нет"

                currentPair.visit == VisitState.VISITED.name -> "Вы уже отметились"

                currentPair.date.toGregorianCalendar() > currentDate -> "Пара еще не началась"

                else -> {
                    val institutionName = parseInstitutionName(currentPair.auditorium)
                    val institution = studentViewModel.getInstitution(institutionName)
                    val locationInst = Location("locationManager")
                    with(locationInst) {
                        latitude = institution.latitude
                        longitude = institution.longitude
                    }
                    when (locationFacade.studentLocation!!.distanceTo(locationInst) <= 100) {
                        true -> {
//                            studentViewModel.changeState(currentPair.date) // fixme
                            "Вы отметились"
                        }
                        false -> "Вы находитесь далеко от института"
                    }
                }
            }

            studentViewModel.showToast(toastText)

            location_tv.text = formatLocation(locationFacade.studentLocation)
            location_progress_bar.visibility = View.INVISIBLE
            check_btn.isClickable = true
        }
    }

    fun onExitBtnClick(v: View) {

        val sp = getSharedPreferences(AUTHENTICATION_SHARED_PREFS, Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        super@StudentActivity.finish()
    }

    // TODO: ну тут явно чет не так
    private fun tabHostCreate() {

        tabhost.setup()

        var tabSpec: TabSpec = tabhost.newTabSpec("tag1")
        tabSpec.setIndicator("Отметка")
        tabSpec.setContent(R.id.tab1)
        tabhost.addTab(tabSpec)

        tabSpec = tabhost.newTabSpec("tag2")
        tabSpec.setIndicator("Опросы")
        tabSpec.setContent(R.id.tab2)
        tabhost.addTab(tabSpec)

        tabhost.setCurrentTabByTag("tag1")
    }

    private fun listViewCreate(studentInfo: StudentInfo) {
        studentViewModel.viewModelScope.launch {

            val allInterviews = studentViewModel.getAllInterviews()
                .filter { it.interviewReference.isValidUrl() }
                .filter { filterStudentInfo(it, studentInfo) }
                .filter { filterInterviewDate(it) }

            interview_rv.adapter = InterviewRecyclerViewAdapter(allInterviews) {
                startActivity(Intent(ACTION_VIEW, Uri.parse(it.interviewReference)))
            }
        }
    }

    private fun classCardCreate() {
        studentViewModel.viewModelScope.launch {

            val schedule = studentViewModel.getSchedule()
            val scheduleOnThisDay = schedule.filter {
                isItCurrentDay(getSplitForStringDate(it.date))
            }

            schedule_rv.adapter = ScheduleRecyclerViewAdapter(scheduleOnThisDay)
        }
    }

    private fun formatLocation(location: Location?) = when (location) {
        null -> ""
        else -> "lat = %1$.6f, lon = %2$.6f".format(location.latitude, location.longitude)
    }

    private fun String.isValidUrl() = URLUtil.isValidUrl(this)
    //.not() // fixme: нужно убрать not(). Оставляю пока, чтобы тестить было легче

    private fun isItCurrentDay(date: List<String>): Boolean =
        date[MONTH].toInt() == (currentDate.get(Calendar.MONTH) + 1)
                && date[DAY].toInt() == currentDate.get(Calendar.DAY_OF_MONTH)

    private fun getSplitForStringDate(date: String): List<String> = date.replace(" ", "").split(",")

    private fun String.toGregorianCalendar(): GregorianCalendar {

        val splittedDate = getSplitForStringDate(this).map(String::toInt)

        return GregorianCalendar(
            splittedDate[YEAR],
            splittedDate[MONTH],
            splittedDate[DAY],
            splittedDate[HOURS],
            splittedDate[MINUTES]
        )
    }

    private fun parseInstitutionName(auditorium: String) = auditorium.split('-').first()

    private fun filterInterviewDate(interview: InterviewEntity): Boolean {
        // Тут по индексации из-за того что есть год идет смещение на одну позицию.
        // Решил не переделывать, потому что переделывание монструозно слишком
        val dateList = interview.time.split(':', '/', ' ', '.').map(String::toInt)
        val dateInterview = GregorianCalendar(
            dateList[YEAR],
            dateList[MONTH],
            dateList[DAY],
            dateList[HOURS],
            dateList[MINUTES]
        )
        return dateInterview > currentDate
    }

    private fun filterStudentInfo(interview: InterviewEntity, studentInfo: StudentInfo) =
        (studentInfo.course.isCourseCorrect(interview.course))
                && (studentInfo.institution.isInstituteCorrect(interview.institution))
                && (studentInfo.studentUnionInfo.isStudentUnionInfoCorrect(interview.studentUnionInfo))

    override fun onRequestPermissionsResult(requestCode: Int, perms: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {

                for (perm in permissions) {
                    if (hasPermission(perm).not()) {
                        permissionsRejected.add(perm)
                    }
                }

                if (permissionsRejected.size > 0) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionsRejected.first())) {

                        AlertDialog.Builder(this).setMessage(
                            "Без этих разрешений приложение будет неработоспособным"
                        ).setPositiveButton("OK") { _, _ ->
                            ActivityCompat.requestPermissions(
                                this, permissionsRejected.toTypedArray(), ALL_PERMISSIONS_RESULT
                            )
                        }.setNegativeButton("Cancel", null).create().show()

                        return
                    }
                }
                else {
                    if (locationFacade.googleApiClient != null) {
                        locationFacade.googleApiClient!!.connect()
                    }
                }
            }
        }
    }

    private fun checkPlayServices(): Boolean {
        val apiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode: Int = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
            }
            else {
                finish()
            }
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()

        registerReceiver(gpsBroadcastReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        if (checkPlayServices().not()) {
            studentViewModel.showToast("Нужно установить Google Play Services")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LocationRequest.PRIORITY_HIGH_ACCURACY -> when (resultCode) {
                Activity.RESULT_OK -> studentViewModel.showToast("onActivityResult: GPS Enabled by user")
                Activity.RESULT_CANCELED -> studentViewModel.showToast("onActivityResult: User rejected GPS request")
                else -> {
                }
            }
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}