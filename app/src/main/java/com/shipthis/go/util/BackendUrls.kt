package com.shipthis.go.util

import com.shipthis.go.BuildConfig

/**
 * Data class representing backend URLs for different services
 */
data class BackendUrls(
    val api: String,
    val web: String,
    val ws: String
)

/**
 * Utility object for managing backend URLs based on domain configuration.
 * Follows the same logic as the JavaScript client.
 */
object BackendUrlProvider {
    
    private const val PRIMARY_DOMAIN = "shipth.is"
    
    /**
     * Gets the configured domain from BuildConfig, falling back to PRIMARY_DOMAIN
     */
    fun getDomain(): String {
        return BuildConfig.SHIPTHIS_DOMAIN.ifEmpty { PRIMARY_DOMAIN }
    }
    
    /**
     * Generates backend URLs based on the provided domain.
     * Follows the same logic as the JavaScript client:
     * - Public domains (containing PRIMARY_DOMAIN) get api. and ws. prefixes
     * - Private domains use the domain directly
     */
    fun getUrlsForDomain(domain: String): BackendUrls {
        val isPublic = domain.contains(PRIMARY_DOMAIN)
        
        val apiDomain = if (isPublic) "api.$domain" else domain
        val wsDomain = if (isPublic) "ws.$domain" else domain
        
        return BackendUrls(
            api = "https://$apiDomain/api/1.0.0",
            web = "https://$domain/",
            ws = "wss://$wsDomain"
        )
    }
    
    /**
     * Gets the backend URLs for the configured domain
     */
    fun getBackendUrls(): BackendUrls {
        return getUrlsForDomain(getDomain())
    }
    
    /**
     * Convenience methods for direct access to specific URLs
     */
    fun getApiUrl(): String = getBackendUrls().api
    fun getWebUrl(): String = getBackendUrls().web
    fun getWsUrl(): String = getBackendUrls().ws
}
