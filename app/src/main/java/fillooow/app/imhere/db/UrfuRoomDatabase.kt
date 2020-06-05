package fillooow.app.imhere.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import fillooow.app.imhere.data.PersonType
import fillooow.app.imhere.data.VisitState
import fillooow.app.imhere.db.dao.AccountDao
import fillooow.app.imhere.db.dao.InstitutionDao
import fillooow.app.imhere.db.dao.InterviewDao
import fillooow.app.imhere.db.dao.ScheduleDao
import fillooow.app.imhere.db.entity.AccountEntity
import fillooow.app.imhere.db.entity.InstitutionEntity
import fillooow.app.imhere.db.entity.InterviewEntity
import fillooow.app.imhere.db.entity.ScheduleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        InterviewEntity::class,
        InstitutionEntity::class,
        ScheduleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UrfuRoomDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun interviewDao(): InterviewDao
    abstract fun institutionDao(): InstitutionDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {

        @Volatile
        private var INSTANCE: UrfuRoomDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): UrfuRoomDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {

                val instance = buildDBInstance(context = context.applicationContext, scope = scope)

                INSTANCE = instance
                return instance
            }
        }

        private fun buildDBInstance(context: Context, scope: CoroutineScope) = Room.databaseBuilder(
            context,
            UrfuRoomDatabase::class.java,
            "urfu_database"
        ).addCallback(AccountDatabaseCallback(scope))
            .addCallback(InstitutionDatabaseCallback(scope))
                .addCallback(ScheduleDatabaseCallback(scope))
                .build()
    }

    private class AccountDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    fillAccountTableWithFakeData(database)
                }
            }
        }

        private suspend fun fillAccountTableWithFakeData(database: UrfuRoomDatabase) {

            for (index in FakeDataHolder.login.indices) {

                val fakeAccount = AccountEntity(

                    login = FakeDataHolder.login[index],
                    password = FakeDataHolder.password[index],
                    personType = FakeDataHolder.login[index].calculatePersonType()
                )

                database.accountDao().insert(fakeAccount)
            }
        }

        private fun String.calculatePersonType() = when (this) {
            "admin" -> PersonType.TEACHER.name
            else -> PersonType.STUDENT.name
        }
    }

    private class InstitutionDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    fillInstitutionTableWithFakeData(database)
                }
            }
        }

        private suspend fun fillInstitutionTableWithFakeData(database: UrfuRoomDatabase) {

            for (index in FakeDataHolder.institutions.indices) {

                val fakeInstitution = InstitutionEntity(

                    institution = FakeDataHolder.institutions[index],
                    latitude = FakeDataHolder.latitude[index],
                    longitude = FakeDataHolder.longitude[index]
                )

                database.institutionDao().insert(fakeInstitution)
            }
        }
    }

    private class ScheduleDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    fillScheduleTableWithFakeData(database)
                }
            }
        }

        private suspend fun fillScheduleTableWithFakeData(database: UrfuRoomDatabase) {

            for (index in FakeDataHolder.date.indices) {

                val fakeSchedule = ScheduleEntity(
                        date = FakeDataHolder.date[index].replace(" ", ""),
                        number = FakeDataHolder.number[index],
                        lecturer = FakeDataHolder.lecturer[index],
                        type = FakeDataHolder.type[index],
                        auditorium = FakeDataHolder.auditorium[index],
                        name = FakeDataHolder.name[index],
                        visit = VisitState.UNVISITED.name
                )

                database.scheduleDao().insert(fakeSchedule)
            }
        }
    }
}