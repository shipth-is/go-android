package com.shipthis.go.data.model

import com.google.gson.annotations.SerializedName

data class SelfWithJWT(
    val jwt: String,
    @SerializedName("createdAt")
    val createdAt: String, // ISO 8601 format
    val details: UserDetails,
    val email: String,
    val id: String,
    @SerializedName("isBetaUser")
    val isBetaUser: Boolean,
    @SerializedName("accountType")
    val accountType: TierTypes,
    @SerializedName("updatedAt")
    val updatedAt: String // ISO 8601 format
)

