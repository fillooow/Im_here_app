package fillooow.app.imhere.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fillooow.app.imhere.R
import fillooow.app.imhere.data.PersonType
import fillooow.app.imhere.utils.AUTHENTICATION_SHARED_PREFS
import fillooow.app.imhere.utils.PERSON_TYPE_SHARED_PREFS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
    }

    override fun onResume() {
        super.onResume()

        GlobalScope.launch {

            delay(1000)

            withContext(Dispatchers.Main) {

                val sp = getSharedPreferences(AUTHENTICATION_SHARED_PREFS, Context.MODE_PRIVATE)

                when (wasUserLoggedIn(sp)) {

                    false -> startLogin()

                    true -> continueWithoutLogin(sp)
                }

                super@PreviewActivity.finish()
            }
        }
    }

    private fun wasUserLoggedIn(sp: SharedPreferences) = sp.contains(AUTHENTICATION_SHARED_PREFS)
            && sp.getBoolean(AUTHENTICATION_SHARED_PREFS, false)

    private fun startLogin() = startActivityIntent(LoginActivity::class.java)

    private fun continueWithoutLogin(sp: SharedPreferences) {

        when (sp.getString(PERSON_TYPE_SHARED_PREFS, PersonType.STUDENT.name) == PersonType.STUDENT.name) {
            true -> startActivityIntent(StudentActivity::class.java)
            else -> startActivityIntent(AddInterviewActivity::class.java)
        }
    }

    private fun startActivityIntent(activity: Class<out Any>) = startActivity(Intent(this, activity))
}