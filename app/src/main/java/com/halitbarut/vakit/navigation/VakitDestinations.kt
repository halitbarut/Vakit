package com.halitbarut.vakit.navigation

sealed class VakitDestination(val route: String) {
    data object Onboarding : VakitDestination("onboarding")
    data object Dashboard : VakitDestination("dashboard")
    data object Statistics : VakitDestination("statistics")
    data object Settings : VakitDestination("settings")
}

val BottomBarDestinations = listOf(
    VakitDestination.Dashboard,
    VakitDestination.Statistics,
)
