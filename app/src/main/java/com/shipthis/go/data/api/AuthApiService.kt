package com.shipthis.go.data.api

import com.shipthis.go.data.model.GDPRRequest
import com.shipthis.go.data.model.Self
import com.shipthis.go.data.model.SelfWithJWT
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/email/send")
    suspend fun requestOtp(@Body request: OtpRequest): OtpResponse

    @POST("auth/email/verify")
    suspend fun verifyOtp(@Body request: OtpVerificationRequest): SelfWithJWT

    @GET("me")
    suspend fun getMe(): Self // Validates JWT, returns user without JWT

    @POST("me/terms")
    suspend fun acceptTerms(@Body request: EmptyBody = EmptyBody()): Self

    @GET("me/gdpr")
    suspend fun getGdprStatus(): List<GDPRRequest>

    @POST("me/gdpr/export")
    suspend fun requestGdprExport(@Body request: EmptyBody = EmptyBody()): Unit

    @POST("me/gdpr/delete")
    suspend fun requestGdprDelete(@Body request: EmptyBody = EmptyBody()): Unit
}

// Request/Response models
data class OtpRequest(
    val email: String
)

data class OtpResponse(
    val status: String // "ok" on success
)

data class OtpVerificationRequest(
    val email: String,
    val otp: String,
    val source: String // e.g., "shipthis-go-1"
)

class EmptyBody {
    // Empty body for POST requests that don't need data
    // Serializes to {} in JSON
}

