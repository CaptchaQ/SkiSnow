package com.skisnow.presentation.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skisnow.presentation.detail.SessionDetailScreen
import com.skisnow.presentation.settings.SettingsScreen

@Composable
fun SkiSnowApp() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Routes.SESSION) {
            composable(Routes.SESSION) {
                SessionScreen(
                    onOpenDetail = { id -> navController.navigate("${Routes.DETAIL}/$id") },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(
                route = "${Routes.DETAIL}/{${Args.SESSION_ID}}",
                arguments = listOf(navArgument(Args.SESSION_ID) { type = NavType.StringType }),
            ) { entry ->
                val sessionId = entry.arguments?.getString(Args.SESSION_ID).orEmpty()
                SessionDetailScreen(
                    sessionId = sessionId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

object Routes {
    const val SESSION = "session"
    const val DETAIL = "detail"
    const val SETTINGS = "settings"
}

object Args {
    const val SESSION_ID = "session_id"
}