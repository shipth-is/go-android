package com.shipthis.go.data.model

import com.google.gson.annotations.SerializedName

enum class GDPRRequestType {
    EXPORT,
    DELETE
}

enum class GDPRRequestStatus {
    PENDING,
    COMPLETED,
    FAILED
}

data class GDPRRequest(
    val id: String,
    val type: GDPRRequestType,
    val status: GDPRRequestStatus,
    val details: Any?,
    @SerializedName("createdAt")
    val createdAt: String, // ISO 8601 format
    @SerializedName("updatedAt")
    val updatedAt: String // ISO 8601 format
)

