package fillooow.app.imhere.activity

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fillooow.app.imhere.data.filter.CourseType
import fillooow.app.imhere.data.filter.InstitutionType
import fillooow.app.imhere.data.filter.StudentInfo
import fillooow.app.imhere.data.filter.StudentUnionType
import fillooow.app.imhere.db.entity.InterviewEntity
import fillooow.app.imhere.extensions.isEnteredDateLessThanCurrent
import fillooow.app.imhere.extensions.textAsString
import fillooow.app.imhere.utils.AUTHENTICATION_SHARED_PREFS
import fillooow.app.imhere.utils.DateMaskedTextChangedListener
import fillooow.app.imhere.vm.AddInterviewViewModel
import fillooow.app.imhere.R
import kotlinx.android.synthetic.main.activity_add_interview.*
import kotlinx.coroutines.launch

class AddInterviewActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener {

    private lateinit var addInterviewViewModel: AddInterviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_interview)

        addInterviewViewModel = ViewModelProvider(this).get(AddInterviewViewModel::class.java)

        setupInterviewDateMask()
        setStateSpinner(all_students_switch.isChecked.not())
        all_students_switch.setOnCheckedChangeListener(this)
    }

    fun onAddInterviewBtnClick(v: View) {
        addInterviewViewModel.viewModelScope.launch {
            val interviewReference = interview_reference_et.textAsString
            val interviewDate = interview_date_et.textAsString

            when {
                interview_author_et.textAsString.isEmpty() -> {
                    showToast("Введите имя автора опроса")
                    return@launch
                }
                interviewDate.isDateValid().not() -> {
                    showToast("Неверный формат даты")
                    return@launch
                }
                interview_date_et.isEnteredDateLessThanCurrent() -> {
                    showToast("Дата дедлайна не может быть раньше сегодняшнего числа")
                    return@launch
                }
                interview_title_et.textAsString.isEmpty() -> {
                    showToast("Введите название опроса")
                    return@launch
                }
                interviewReference.isEmpty() -> {
                    showToast("Укажите ссылку")
                    return@launch
                }
                interviewReference.isReferenceValid().not() -> {
                    showToast("Ссылка некорректна")
                    return@launch
                }
            }

            val filterInfo = getStudentFilter()

            val interview = InterviewEntity(
                interviewReference = interviewReference,
                interviewer = interview_author_et.textAsString,
                title = interview_title_et.textAsString,
                course = filterInfo.course.description,
                institution = filterInfo.institution.description,
                studentUnionInfo = filterInfo.studentUnionInfo.description,
                time = "${interview_date_et.textAsString} 23:59"
            )

            try {
                addInterviewViewModel.insertInterview(interview)
            } catch (error: SQLiteConstraintException) {
                showToast("Опрос уже существует")
                return@launch
            }
            showToast("Опрос успешно добавлен")
        }
    }

    fun onExitInterviewBtnClick(v: View) {

        val sp = getSharedPreferences(AUTHENTICATION_SHARED_PREFS, Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java))
        this.finish()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) = when (isChecked) {

        true -> setStateSpinner(false)
        false -> setStateSpinner(true)
    }

    private fun setStateSpinner(state: Boolean) {

        courses_spinner.isEnabled = state
        institutions_spinner.isEnabled = state
        students_union_spinner.isEnabled = state
    }

    //Проверка ссылки на форму
    private fun String.isReferenceValid(): Boolean {

        val shortGoogleFromPattern = """https://forms.gle/.+"""
        val longGoogleFromPattern = """https://docs.google.com/forms/d/.*"""
        val googleFormRegex = Regex("$shortGoogleFromPattern|$longGoogleFromPattern")
        return matches(googleFormRegex)
//            .not() //fixme: убрать,  для тестов сделано так
    }

    private fun String.isDateValid() = matches(Regex("""\d{1,2}.\d{1,2}.20\d{2}"""))
//        .not() //fixme: убрать,  для тестов сделано так

    //Получение фильтра для выбора получателей
    private fun getStudentFilter() = when (all_students_switch.isChecked) {

        true -> StudentInfo(
            course = CourseType.ALL_COURSES,
            institution = InstitutionType.ALL_INSTITUTIONS,
            studentUnionInfo = StudentUnionType.ALL_STUDENTS
        )
        else -> StudentInfo(
            course = CourseType.findCourseByDescription(courses_spinner.selectedItem.toString()),
            institution = InstitutionType.findInstituteByDescription(institutions_spinner.selectedItem.toString()),
            studentUnionInfo = StudentUnionType.findStudentUnionInfoByDescription(
                students_union_spinner.selectedItem.toString()
            )
        )
    }

    private fun setupInterviewDateMask() = DateMaskedTextChangedListener().installListener(interview_date_et) {
        showToast("Дата дедлайна не может быть раньше сегодняшнего числа")
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}