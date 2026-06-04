package com.example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.components.CurbFlowAppShell

@Composable
fun MainApp() {
    val navController = rememberNavController()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    CurbFlowAppShell(
        currentRoute = currentRoute,
        onNavigate = { route ->
            if (currentRoute != route) {
                navController.navigate(route) {
                    popUpTo("map") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "onboarding",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) },
            exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) },
            popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) },
            popExitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) }
        ) {
            composable("onboarding") { OnboardingScreen(onFinish = { navController.navigate("map") { popUpTo("onboarding") { inclusive = true } } }) }
            composable("map") { MapScreen(navController) }
            composable("zones") { ZoneListScreen(navController) }
            // Let "route" base navigate to a default screen or list if no param provided, or we can just point to MapScreen or something
            composable("route") { MapScreen(navController) } // Fallback to map if no zone selected
            composable("route/{zoneId}") { backStackEntry -> 
                val zoneId = backStackEntry.arguments?.getString("zoneId") ?: ""
                RouteScreen(navController, zoneId) 
            }
            composable("turn_by_turn/{zoneId}") { backStackEntry ->
                val zoneId = backStackEntry.arguments?.getString("zoneId") ?: ""
                TurnByTurnScreen(navController, zoneId)
            }
            composable("private") { PrivateParkingScreen(navController) }
            composable("fleet") { FleetScreen(navController) }
            composable("city") { MunicipalScreen(navController) }
            composable("privacy") { PrivacyScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
        }
    }
}
