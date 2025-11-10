package com.shipthis.go.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.shipthis.go.data.api.AuthApiService
import com.shipthis.go.data.model.Self
import com.shipthis.go.data.model.SelfWithJWT
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val authApiService: AuthApiService
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // Use a scope for background operations
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<SelfWithJWT?>(null)
    val currentUser: StateFlow<SelfWithJWT?> = _currentUser.asStateFlow()

    init {
        // Load stored user first (synchronously)
        val storedUser = getStoredUser()
        if (storedUser != null) {
            _currentUser.value = storedUser
            _isLoggedIn.value = true
        }

        // Then validate JWT asynchronously
        repositoryScope.launch {
            validateStoredSession()
        }
    }

    private fun getStoredUser(): SelfWithJWT? {
        val userJson = prefs.getString("user_data", null) ?: return null
        return try {
            gson.fromJson(userJson, SelfWithJWT::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun validateStoredSession() {
        val storedUser = getStoredUser()
        if (storedUser == null) {
            _isLoggedIn.value = false
            _currentUser.value = null
            return
        }
        
        // Validate JWT by calling /me endpoint
        try {
            val currentSelf: Self = authApiService.getMe()
            // If successful, update user data (except JWT which we keep from stored)
            val updatedUser = storedUser.copy(
                createdAt = currentSelf.createdAt,
                details = currentSelf.details,
                email = currentSelf.email,
                id = currentSelf.id,
                updatedAt = currentSelf.updatedAt
            )
            saveUserInternal(updatedUser)
            _isLoggedIn.value = true
        } catch (e: Exception) {
            // JWT is invalid (401 or other error) - clear session
            clearSession()
        }
    }

    fun saveUser(user: SelfWithJWT) {
        saveUserInternal(user)
        _isLoggedIn.value = true
    }

    private fun saveUserInternal(user: SelfWithJWT) {
        val userJson = gson.toJson(user)
        prefs.edit()
            .putString("user_data", userJson)
            .apply()
        _currentUser.value = user
    }

    fun logout() {
        clearSession()
    }

    private fun clearSession() {
        prefs.edit()
            .remove("user_data")
            .apply()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun getJwt(): String? = _currentUser.value?.jwt

    // Call this when we get a 401 to clear invalid JWT
    fun handleUnauthorized() {
        clearSession()
    }
}

