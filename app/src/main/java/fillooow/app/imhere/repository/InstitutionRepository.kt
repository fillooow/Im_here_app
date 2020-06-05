package fillooow.app.imhere.repository

import fillooow.app.imhere.db.dao.InstitutionDao
import fillooow.app.imhere.db.entity.InstitutionEntity

class InstitutionRepository(
        private val institutionDao: InstitutionDao
) {
    suspend fun getCoordinates(prefix: String) : List<InstitutionEntity> = institutionDao.getCoordinates(prefix)
}