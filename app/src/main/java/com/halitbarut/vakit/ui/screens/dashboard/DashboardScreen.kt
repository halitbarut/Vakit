package com.halitbarut.vakit.ui.screens.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Bugünkü İlerlemen",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    FilledIconButton(
                        onClick = onNavigateToSettings,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Ayarlar",
                        )
                    }
                },
            )
        },
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
        Surface(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(horizontal = 20.dp)) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                hasData -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        SummaryCard(
                            totalRemaining = state.totalRemaining,
                            totalCompletedToday = state.totalCompletedToday,
                        )
                        PrayerGrid(
                            cards = state.prayerCards,
                            onIncreaseDebt = onIncreaseDebt,
                            onComplete = onComplete,
                            onEdit = onEdit,
                        )
                        EstimatedFinishCard(
                            estimatedDate = state.estimatedFinishDate,
                            averageDaily = state.averageDailyCompletion,
                            totalRemaining = state.totalRemaining,
                            bestDailyCompletion = state.bestDailyCompletion,
                        )
                    }
                }
                else -> {
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

@Composable
private fun SummaryCard(
    totalRemaining: Int,
    totalCompletedToday: Int,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryColumn(
                title = "Toplam Kalan",
                value = formatCount(totalRemaining),
                valueColor = MaterialTheme.colorScheme.primary,
            )
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .height(56.dp)
                    .width(1.dp),
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cards, key = { it.prayerType.name }) { card ->
                PrayerCard(
                    card = card,
                    onIncreaseDebt = onIncreaseDebt,
                    onComplete = onComplete,
                    onEdit = onEdit,
                )
            }
        }
    }
}

@Composable
private fun PrayerCard(
    card: PrayerCardUiState,
    onIncreaseDebt: (PrayerType) -> Unit,
    onComplete: (PrayerType) -> Unit,
    onEdit: (PrayerType) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = card.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${card.remainingText} kaldı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = card.lastUpdateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onEdit(card.prayerType) }) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Borcu düzenle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = { onComplete(card.prayerType) },
                        enabled = card.isIncrementEnabled,
                        colors = IconButtonDefaults.filledIconButtonColors(
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
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Bir ekle",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EstimatedFinishCard(
    estimatedDate: String?,
    averageDaily: Int,
    totalRemaining: Int,
    bestDailyCompletion: Int,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Tahmini Bitiş Tarihi",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = estimatedDate ?: "Tempo belirlemek için birkaç vakit ekleyin",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                EstimatedMetric(
                    label = "Günlük Ortalama",
                    value = formatCount(averageDaily),
                )
                EstimatedMetric(
                    label = "En İyi Gün",
                    value = formatCount(bestDailyCompletion),
                )
                EstimatedMetric(
                    label = "Kalan Toplam",
                    value = formatCount(totalRemaining),
                )
            }
        }
    }
}

@Composable
private fun EstimatedMetric(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyDashboardState(onNavigateToOnboarding: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
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
