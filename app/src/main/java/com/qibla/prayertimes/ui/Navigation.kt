package com.qibla.prayertimes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private object Routes {
    const val HOME = "home"
    const val ALARMS = "alarms"
    const val ABOUT = "about"
    const val MONTHLY = "monthly"
}

@Composable
fun QiblaNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            QiblaScreen(
                onOpenAlarms = { navController.navigate(Routes.ALARMS) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                onOpenMonthly = { navController.navigate(Routes.MONTHLY) }
            )
        }
        composable(Routes.ALARMS) {
            AlarmSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.MONTHLY) {
            MonthlyTimesScreen(onBack = { navController.popBackStack() })
        }
    }
}
