package com.halitbarut.vakit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.halitbarut.vakit.navigation.VakitDestination

@Composable
fun VakitBottomBar(
    currentDestination: VakitDestination,
    onDestinationSelected: (VakitDestination) -> Unit,
) {
    NavigationBar {
        bottomBarItems.forEach { item ->
            val selected = item.destination::class == currentDestination::class
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(item.destination) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

private data class BottomBarItem(
    val destination: VakitDestination,
    val label: String,
    val icon: ImageVector,
)

private val bottomBarItems = listOf(
    BottomBarItem(
        destination = VakitDestination.Dashboard,
        label = "Ana Ekran",
        icon = Icons.Outlined.Home,
    ),
    BottomBarItem(
        destination = VakitDestination.Statistics,
        label = "İstatistikler",
        icon = Icons.Outlined.BarChart,
    ),
)
