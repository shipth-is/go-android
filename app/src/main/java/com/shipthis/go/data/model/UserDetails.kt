package com.shipthis.go.data.model

import com.google.gson.annotations.SerializedName

data class UserDetails(
    @SerializedName("hasAcceptedTerms")
    val hasAcceptedTerms: Boolean? = null,
    val source: String? = null,
    @SerializedName("termsAgreementVersionId")
    val termsAgreementVersionId: String? = null,
    @SerializedName("privacyAgreementVersionId")
    val privacyAgreementVersionId: String? = null
)

