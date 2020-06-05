package fillooow.app.imhere.vm

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fillooow.app.imhere.db.UrfuRoomDatabase
import fillooow.app.imhere.db.entity.AccountEntity
import fillooow.app.imhere.repository.AccountRepository
import fillooow.app.imhere.utils.AUTHENTICATION_SHARED_PREFS
import fillooow.app.imhere.utils.PERSON_TYPE_SHARED_PREFS

class LoginViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repository: AccountRepository

    private val db: UrfuRoomDatabase by lazy {

        UrfuRoomDatabase.getDatabase(
            context = app,
            scope = viewModelScope
        )
    }

    init {

        val accountDao = db.accountDao()

        repository = AccountRepository(accountDao)
    }

    /**
     * В Room колбек onCreate дергается только после обращения к бд
     * с действием на чтение/запись. Поэтому, имитируем обращение, чтобы
     * дать заполниться фейковым данным. Если этого не сделать, то к моменту самого первого
     * обращения к бд (например - с помощью [getAccountByLogin]) мы получим null, так как
     * бд будет пустой, и только после этого самого обращения бд заполнится значениями.
     *
     * https://stackoverflow.com/questions/48280941/room-database-force-oncreate-callback
     */
    fun fillDatabaseWithFakeInfo() {
        db.runInTransaction {
            // имитируем работу с бд, так как она заполняется фейковыми данными только после обращения к ней
        }
    }

    suspend fun getAccountByLogin(login: String) = repository.getAccountByLogin(login)

    fun saveAccountToSharedPrefs(account: AccountEntity) {

        val sp = app.getSharedPreferences(AUTHENTICATION_SHARED_PREFS, Context.MODE_PRIVATE)

        with(sp.edit()) {
            putBoolean(AUTHENTICATION_SHARED_PREFS, true)
            putString(PERSON_TYPE_SHARED_PREFS, account.personType)
            apply()
        }
    }
}