package com.shipthis.go.data.api

import com.shipthis.go.data.model.GoBuild
import retrofit2.http.GET
import retrofit2.http.Path

interface GoBuildApiService {
    @GET("go/{buildId}")
    suspend fun getGoBuild(@Path("buildId") buildId: String): GoBuild
}
