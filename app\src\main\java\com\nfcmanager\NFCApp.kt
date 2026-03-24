package com.nfcmanager

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nfcmanager.ui.screen.DataScreen
import com.nfcmanager.ui.screen.MainScreen
import com.nfcmanager.ui.screen.ReadScreen
import com.nfcmanager.ui.screen.SettingsScreen

/**
 * 应用主入口，负责导航管理
 */
@Composable
fun NFCApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        // 主界面
        composable(Screen.Main.route) {
            MainScreen(
                onReadNFC = { navController.navigate(Screen.Read.route) },
                onViewData = { navController.navigate(Screen.Data.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        // 读取界面
        composable(Screen.Read.route) {
            ReadScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        // 数据界面
        composable(Screen.Data.route) {
            DataScreen(
                onBack = { navController.navigateUp() }
            )
        }
        
        // 设置界面
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }
    }
}

/**
 * 应用屏幕定义
 */
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Read : Screen("read")
    object Data : Screen("data")
    object Settings : Screen("settings")
}