package com.shipthis.go.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shipthis.go.data.repository.AuthRepository
import com.shipthis.go.ui.screens.home.HomeScreen
import com.shipthis.go.ui.screens.login.LoginScreen
import com.shipthis.go.ui.screens.login.OtpVerificationScreen

@Composable
fun ShipThisGoNavigation(
    navController: NavHostController,
    authRepository: AuthRepository,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by authRepository.isLoggedIn.collectAsState()

    // Determine start destination based on auth state
    val startDestination = if (isLoggedIn) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onOtpRequested = { email ->
                    val encodedEmail = android.net.Uri.encode(email)
                    navController.navigate("otp_verification/$encodedEmail")
                }
            )
        }

        composable("otp_verification/{email}") { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = android.net.Uri.decode(encodedEmail)
            OtpVerificationScreen(
                email = email,
                onVerificationSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToEmail = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen()
        }
    }

    // Handle auth state changes - if logged out, navigate to login
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
