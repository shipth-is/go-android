package com.shipthis.go.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a GoBuild from the ShipThis API
 * Based on the PublicGoBuild type from the server
 */
data class GoBuild(
    val id: String,
    @SerializedName("jobId")
    val jobId: String,
    @SerializedName("projectId")
    val projectId: String,
    val platform: String, // "GO"
    @SerializedName("buildType")
    val buildType: String, // "GO"
    val details: Any?, // Can be string, number, boolean, JsonObject, or JsonArray
    val url: String,
    @SerializedName("isFound")
    val isFound: Boolean?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("jobDetails")
    val jobDetails: JobDetails,
    val project: Project
)

data class JobDetails(
    // Required fields from ProjectDetails
    @SerializedName("buildNumber")
    val buildNumber: Int,
    @SerializedName("semanticVersion")
    val semanticVersion: String,
    @SerializedName("gameEngine")
    val gameEngine: String,
    @SerializedName("gameEngineVersion")
    val gameEngineVersion: String,
    
    // UploadDetails fields
    @SerializedName("gitCommitHash")
    val gitCommitHash: String? = null,
    @SerializedName("gitBranch")
    val gitBranch: String? = null,
    @SerializedName("zipFileMd5")
    val zipFileMd5: String? = null,
    
    // JobDetails specific fields
    @SerializedName("skipPublish")
    val skipPublish: Boolean? = null,
    @SerializedName("verbose")
    val verbose: Boolean? = null
)

data class Project(
    val id: String,
    val name: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    val details: ProjectDetails
)

data class ProjectDetails(
    @SerializedName("gameEngine")
    val gameEngine: String?,
    @SerializedName("gameEngineVersion")
    val gameEngineVersion: String?,
    @SerializedName("iosBundleId")
    val iosBundleId: String?,
    @SerializedName("androidPackageName")
    val androidPackageName: String?,
    @SerializedName("buildNumber")
    val buildNumber: Int?,
    @SerializedName("semanticVersion")
    val semanticVersion: String?,
    @SerializedName("gcpProjectId")
    val gcpProjectId: String?,
    @SerializedName("gcpServiceAccountId")
    val gcpServiceAccountId: String?,
    @SerializedName("googlePlayDeveloperId")
    val googlePlayDeveloperId: String?
)
