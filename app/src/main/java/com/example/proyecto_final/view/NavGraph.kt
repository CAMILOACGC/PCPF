package com.example.proyecto_final.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object RegisterMotorcycle : Screen("register")
    object Maintenance : Screen("maintenance")
    object Documents : Screen("documents")
    object GPS : Screen("gps")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Dashboard.route) }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.RegisterMotorcycle.route) {
            RegisterMotorcycleScreen(navController)
        }
        composable(Screen.Maintenance.route) {
            MaintenanceScreen(navController)
        }
        composable(Screen.Documents.route) {
            DocumentsScreen(navController)
        }
        composable(Screen.GPS.route) {
            GPSScreen(navController)
        }
    }
}
