package fillooow.app.imhere.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fillooow.app.imhere.R
import fillooow.app.imhere.data.PersonType
import fillooow.app.imhere.db.entity.AccountEntity
import fillooow.app.imhere.vm.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        loginViewModel.viewModelScope.launch(Dispatchers.IO) {
            loginViewModel.fillDatabaseWithFakeInfo()
        }
    }

    /**
     * По клику - проверка данных с данными в базе данных
     * Механизм на случай, если не выйдет с ЛК
     * + готовая форма аутентификации
     * */
    fun onLoginBtnClick(v: View) {
        loginViewModel.viewModelScope.launch {

            val login = login_et.text.toString()
            val account = loginViewModel.getAccountByLogin(login)

            if (account.isAccountValid().not()) return@launch

            loginViewModel.saveAccountToSharedPrefs(account)

            val activity = when (account.personType) {
                PersonType.TEACHER.name -> AddInterviewActivity::class.java
                else -> StudentActivity::class.java
            }
            startActivityIntent(activity)
            super@LoginActivity.finish()
        }
    }

    private fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

    private fun startActivityIntent(activity: Class<out Any>) = startActivity(Intent(this, activity))

    private fun AccountEntity?.isAccountValid(): Boolean = when {

        this == null -> {
            showToast("Неверный логин")
            false
        }

        this.password != getIntroducedPasswordHashCode() -> {
            showToast("Неверный пароль")
            false
        }

        else -> true
    }

    private fun getIntroducedPasswordHashCode() = password_et.text.toString().hashCode()
}