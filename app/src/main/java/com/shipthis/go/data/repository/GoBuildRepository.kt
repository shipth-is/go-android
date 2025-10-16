package com.shipthis.go.data.repository

import com.shipthis.go.data.api.GoBuildApiService
import com.shipthis.go.data.model.GoBuild
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoBuildRepository @Inject constructor(
    private val apiService: GoBuildApiService
) {
    suspend fun getGoBuild(buildId: String): GoBuild {
        return apiService.getGoBuild(buildId)
    }
}
