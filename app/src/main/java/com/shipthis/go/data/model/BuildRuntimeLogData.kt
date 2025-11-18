package com.shipthis.go.data.model

import com.google.gson.annotations.SerializedName

data class BuildRuntimeLogData(
    @SerializedName("buildId")
    val buildId: String,
    val level: String,
    val message: String,
    val details: Any? = null,
    @SerializedName("sentAt")
    val sentAt: String? = null,
    val sequence: Long? = null
)

