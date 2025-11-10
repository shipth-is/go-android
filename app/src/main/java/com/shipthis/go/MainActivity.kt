package com.shipthis.go

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.shipthis.go.data.repository.AuthRepository
import com.shipthis.go.ui.navigation.ShipThisGoNavigation
import com.shipthis.go.ui.theme.ShipThisGoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShipThisGoTheme {
                ShipThisGoApp(authRepository = authRepository)
            }
        }
    }
}

@Composable
fun ShipThisGoApp(authRepository: AuthRepository) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        ShipThisGoNavigation(
            navController = navController,
            authRepository = authRepository,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
