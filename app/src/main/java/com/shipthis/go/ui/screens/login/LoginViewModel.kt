package com.shipthis.go.ui.screens.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shipthis.go.BuildConfig
import com.shipthis.go.data.api.AuthApiService
import com.shipthis.go.data.api.OtpRequest
import com.shipthis.go.data.api.OtpVerificationRequest
import com.shipthis.go.data.repository.AuthRepository
import com.shipthis.go.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authApiService: AuthApiService,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val source: String = "shipthis-go-${BuildConfig.VERSION_NAME}"

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun updateOtpCode(code: String) {
        // Restrict to 6 digits only
        val digitsOnly = code.filter { it.isDigit() }.take(6)
        _uiState.value = _uiState.value.copy(otpCode = digitsOnly, error = null)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Step 1: Request OTP
    fun requestOtp(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()

        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email cannot be empty")
            return
        }

        if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = authApiService.requestOtp(OtpRequest(email))
                if (response.status == "ok") {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = LoginStep.OTP_VERIFICATION
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to send code"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorHandler.getErrorMessage(e)
                )
            }
        }
    }

    // Step 2: Verify OTP
    fun verifyOtp(email: String, onSuccess: () -> Unit) {
        val otp = _uiState.value.otpCode.trim()

        if (email.isBlank() || otp.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email and code are required")
            return
        }

        if (otp.length != 6) {
            _uiState.value = _uiState.value.copy(error = "Code must be 6 digits")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val user = authApiService.verifyOtp(
                    OtpVerificationRequest(
                        email = email.trim(),
                        otp = otp,
                        source = source
                    )
                )
                authRepository.saveUser(user)
                
                // Accept terms after successful login
                try {
                    authApiService.acceptTerms()
                } catch (e: Exception) {
                    // Log error but don't block login flow
                    // Terms acceptance failure shouldn't prevent user from logging in
                }
                
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorHandler.getErrorMessage(e)
                )
            }
        }
    }
    
    fun goBackToEmail() {
        _uiState.value = _uiState.value.copy(
            step = LoginStep.EMAIL_ENTRY,
            otpCode = "",
            error = null
        )
    }
}

enum class LoginStep {
    EMAIL_ENTRY,
    OTP_VERIFICATION
}

data class LoginUiState(
    val email: String = "",
    val otpCode: String = "",
    val step: LoginStep = LoginStep.EMAIL_ENTRY,
    val isLoading: Boolean = false,
    val error: String? = null
)

