package com.halitbarut.vakit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.halitbarut.vakit.ui.screens.dashboard.DashboardRoute
import com.halitbarut.vakit.ui.screens.onboarding.OnboardingRoute
import com.halitbarut.vakit.ui.screens.settings.SettingsRoute
import com.halitbarut.vakit.ui.screens.statistics.StatisticsRoute

@Composable
fun VakitNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = VakitDestination.Onboarding.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        onboardingGraph(navController)
        composable(route = VakitDestination.Dashboard.route) {
            DashboardRoute(
                onNavigateToSettings = {
                    navController.navigate(VakitDestination.Settings.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(VakitDestination.Statistics.route)
                },
                onNavigateToOnboarding = {
                    navController.navigate(VakitDestination.Onboarding.route) {
                        popUpTo(VakitDestination.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(route = VakitDestination.Statistics.route) {
            StatisticsRoute(
                onNavigateToDashboard = {
                    navController.navigate(VakitDestination.Dashboard.route) {
                        popUpTo(VakitDestination.Dashboard.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(route = VakitDestination.Settings.route) {
            SettingsRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun NavGraphBuilder.onboardingGraph(navController: NavHostController) {
    composable(route = VakitDestination.Onboarding.route) {
        OnboardingRoute(
            onCompleted = {
                navController.navigate(VakitDestination.Dashboard.route) {
                    popUpTo(VakitDestination.Onboarding.route) { inclusive = true }
                }
            },
        )
    }
}
