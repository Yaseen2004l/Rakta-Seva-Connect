package com.raktaseva.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raktaseva.app.ui.screens.*
import com.raktaseva.app.ui.theme.RaktaSevaTheme
import com.raktaseva.app.viewmodel.DonorViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: DonorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        val prefs        = getSharedPreferences("rakta_prefs", Context.MODE_PRIVATE)
        val savedDonorId = prefs.getString("donor_id", null)
        if (savedDonorId != null) viewModel.loadDonor(savedDonorId)

        setContent {
            RaktaSevaTheme {
                RaktaSevaApp(
                    viewModel         = viewModel,
                    onDonorRegistered = { donorId ->
                        prefs.edit().putString("donor_id", donorId).apply()
                    }
                )
            }
        }
    }
}

@Composable
fun RaktaSevaApp(
    viewModel: DonorViewModel,
    onDonorRegistered: (String) -> Unit
) {
    val navController  = rememberNavController()
    var showSplash     by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val state by viewModel.uiState.collectAsState()

    NavHost(
        navController    = navController,
        startDestination = "home",
        enterTransition  = { slideInHorizontally { it } + fadeIn() },
        exitTransition   = { slideOutHorizontally { -it } + fadeOut() },
        popEnterTransition  = { slideInHorizontally { -it } + fadeIn() },
        popExitTransition   = { slideOutHorizontally { it } + fadeOut() }
    ) {

        composable("home") {
            HomeScreen(
                viewModel   = viewModel,
                onNeedBlood = { navController.navigate("request") },
                onDonate    = { navController.navigate("register") },
                onProfile   = { navController.navigate("profile") },
                onRequests  = { navController.navigate("requests") }
            )
        }

        composable("register") {
            DonorRegistrationScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() },
                onSuccess = { donorId ->
                    onDonorRegistered(donorId)
                    navController.navigate("profile") { popUpTo("home") }
                }
            )
        }

        composable("request") {
            RequestScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel  = viewModel,
                onBack     = { navController.popBackStack() },
                onRegister = { navController.navigate("register") }
            )
        }

        composable("requests") {
            RequestsScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() },
                onViewRequest = { request ->
                    // Store selected request and navigate to alert screen
                    viewModel.selectRequest(request)
                    navController.navigate("donor_alert")
                }
            )
        }

        composable("donor_alert") {
            val request = state.selectedRequest
            if (request != null) {
                DonorAlertScreen(
                    request   = request,
                    viewModel = viewModel,
                    onBack    = { navController.popBackStack() }
                )
            }
        }
    }
}