package com.shipthis.go.data.repository

import com.shipthis.go.data.api.SampleApiService
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleRepository @Inject constructor(
    private val apiService: SampleApiService
) {
    suspend fun getSampleData(): String {
        // Simulate network delay
        delay(2000)
        
        // In a real app, you would call the API service here
        // return apiService.getSampleData()
        
        // For now, return mock data
        return "Sample data loaded successfully!"
    }
}
