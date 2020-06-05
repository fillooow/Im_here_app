package fillooow.app.imhere.repository

import fillooow.app.imhere.db.dao.AccountDao

class AccountRepository(
    private val accountDao: AccountDao
) {

    suspend fun getAccountByLogin(login: String) = accountDao.getAccountByLogin(login)
}