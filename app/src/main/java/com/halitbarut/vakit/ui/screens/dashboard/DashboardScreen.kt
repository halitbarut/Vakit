package com.halitbarut.vakit.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.navigation.VakitDestination
import com.halitbarut.vakit.ui.components.EditPrayerDialog
import com.halitbarut.vakit.ui.components.VakitBottomBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        state = uiState,
        onIncreaseDebt = viewModel::onIncreaseDebtClicked,
        onComplete = viewModel::onCompletedClicked,
        onEdit = viewModel::onEditClicked,
        onEditValueChange = viewModel::onEditValueChanged,
        onEditDismiss = viewModel::onEditDismissed,
        onEditConfirm = viewModel::onEditConfirmed,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToOnboarding = onNavigateToOnboarding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onIncreaseDebt: (PrayerType) -> Unit,
    onComplete: (PrayerType) -> Unit,
    onEdit: (PrayerType) -> Unit,
    onEditValueChange: (String) -> Unit,
    onEditDismiss: () -> Unit,
    onEditConfirm: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    val hasData = state.prayerCards.isNotEmpty()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = { DashboardTopBar(onNavigateToSettings) },
        bottomBar = {
            VakitBottomBar(
                currentDestination = VakitDestination.Dashboard,
                onDestinationSelected = { destination ->
                    when (destination) {
                        VakitDestination.Dashboard -> Unit
                        VakitDestination.Statistics -> onNavigateToStatistics()
                        else -> Unit
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            hasData -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        SummaryCard(
                            totalRemaining = state.totalRemaining,
                            totalCompletedToday = state.totalCompletedToday,
                        )
                    }
                    item {
                        PrayerGrid(
                            cards = state.prayerCards,
                            onIncreaseDebt = onIncreaseDebt,
                            onComplete = onComplete,
                            onEdit = onEdit,
                        )
                    }
                    item {
                        EstimatedFinishCard(estimatedDate = state.estimatedFinishDate)
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                ) {
                    EmptyDashboardState(onNavigateToOnboarding)
                }
            }
        }
    }

    state.editDialog?.let { dialogState ->
        EditPrayerDialog(
            state = dialogState,
            onValueChange = onEditValueChange,
            onDismiss = onEditDismiss,
            onConfirm = onEditConfirm,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onNavigateToSettings: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Bugünkü İlerlemen",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.padding(end = 4.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Ayarlar",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    )
}

@Composable
private fun SummaryCard(
    totalRemaining: Int,
    totalCompletedToday: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryColumn(
                title = "Toplam Kalan",
                value = formatCount(totalRemaining),
                valueColor = MaterialTheme.colorScheme.primary,
            )
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
            )
            SummaryColumn(
                title = "Bugün Kıldın",
                value = formatCount(totalCompletedToday),
                valueColor = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun RowScope.SummaryColumn(
    title: String,
    value: String,
    valueColor: Color,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = valueColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PrayerGrid(
    cards: List<PrayerCardUiState>,
    onIncreaseDebt: (PrayerType) -> Unit,
    onComplete: (PrayerType) -> Unit,
    onEdit: (PrayerType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Hızlı Ekle",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            cards.chunked(2).forEach { rowCards ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowCards.forEach { card ->
                        PrayerCard(
                            card = card,
                            onIncreaseDebt = onIncreaseDebt,
                            onComplete = onComplete,
                            onEdit = onEdit,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowCards.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerCard(
    card: PrayerCardUiState,
    onIncreaseDebt: (PrayerType) -> Unit,
    onComplete: (PrayerType) -> Unit,
    onEdit: (PrayerType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = card.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onEdit(card.prayerType) },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Borcu düzenle",
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${card.remainingText} kaldı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = { onComplete(card.prayerType) },
                        modifier = Modifier.size(32.dp),
                        enabled = card.isIncrementEnabled,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = "Bir eksilt",
                        )
                    }
                    FilledIconButton(
                        onClick = { onIncreaseDebt(card.prayerType) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Bir ekle",
                        )
                    }
                }
            }
            Text(
                text = card.lastUpdateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EstimatedFinishCard(estimatedDate: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Tahmini Bitiş Tarihi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = estimatedDate ?: "Tempo belirlemek için birkaç vakit ekleyin",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun EmptyDashboardState(onNavigateToOnboarding: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Henüz kayıtlı borç yok",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Hızlıca başlangıç ekranına dönerek borç bilgilerini girebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onNavigateToOnboarding) {
            Text("Başlangıca Git")
        }
    }
}

private fun formatCount(value: Int): String = NumberFormat.getIntegerInstance(Locale("tr"))
    .format(value.coerceAtLeast(0))
