package com.nfcmanager

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfcmanager.ui.screen.DataScreen
import com.nfcmanager.ui.screen.EmulationScreen
import com.nfcmanager.ui.screen.MainScreen
import com.nfcmanager.ui.screen.ReadScreen
import com.nfcmanager.ui.screen.SettingsScreen

@Composable
fun NFCApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onReadNFC = { navController.navigate(Screen.Read.route) },
                onViewData = { navController.navigate(Screen.Data.route) },
                onEmulation = { navController.navigate(Screen.Emulation.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Read.route) {
            ReadScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        composable(Screen.Data.route) {
            DataScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        composable(Screen.Emulation.route) {
            EmulationScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Read : Screen("read")
    object Data : Screen("data")
    object Emulation : Screen("emulation")
    object Settings : Screen("settings")
}
