package com.shipthis.go.data.repository

import com.shipthis.go.data.api.GoBuildApiService
import com.shipthis.go.data.local.GoBuildDao
import com.shipthis.go.data.model.GoBuild
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoBuildRepository @Inject constructor(
    private val apiService: GoBuildApiService,
    private val goBuildDao: GoBuildDao
) {
    /**
     * Fetch a GoBuild from the API
     */
    suspend fun getGoBuild(buildId: String): GoBuild {
        return apiService.getGoBuild(buildId)
    }
    
    /**
     * Save a GoBuild to the local database
     * Duplicates are ignored (based on primary key)
     */
    suspend fun saveGoBuild(goBuild: GoBuild) {
        goBuildDao.insertGoBuild(goBuild)
    }
    
    /**
     * Get all GoBuilds from the local database (newest first)
     */
    fun getAllGoBuilds(): Flow<List<GoBuild>> {
        return goBuildDao.getAllGoBuilds()
    }
    
    /**
     * Get a GoBuild by ID from the local database
     */
    suspend fun getGoBuildById(id: String): GoBuild? {
        return goBuildDao.getGoBuildById(id)
    }
}
