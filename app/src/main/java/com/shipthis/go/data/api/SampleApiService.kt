package com.shipthis.go.data.api

import retrofit2.http.GET

interface SampleApiService {
    @GET("sample")
    suspend fun getSampleData(): String
}
