package com.halitbarut.vakit.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halitbarut.vakit.domain.model.PrayerType
import com.halitbarut.vakit.navigation.VakitDestination
import com.halitbarut.vakit.ui.components.VakitBottomBar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StatisticsRoute(
    onNavigateToDashboard: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatisticsScreen(
        state = uiState,
        onNavigateToDashboard = onNavigateToDashboard,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onNavigateToDashboard: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("İstatistikler") })
        },
        bottomBar = {
            VakitBottomBar(
                currentDestination = VakitDestination.Statistics,
                onDestinationSelected = { destination ->
                    when (destination) {
                        VakitDestination.Dashboard -> onNavigateToDashboard()
                        VakitDestination.Statistics -> Unit
                        else -> Unit
                    }
                },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                !state.hasData -> {
                    EmptyStatisticsState()
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        WeeklyPerformanceCard(state.weeklyPerformance)
                        MetricsRow(
                            averageDaily = state.averageDailyCompletion,
                            bestDaily = state.bestDailyCompletion,
                            totalPrayed = state.totalPrayed,
                        )
                        DistributionCard(state)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyPerformanceCard(weeklyPerformance: List<WeeklyPerformanceBar>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Haftalık Performans",
                style = MaterialTheme.typography.titleMedium,
            )
            WeeklyBarChart(bars = weeklyPerformance)
            Text(
                text = "Toplam Kılınan Sayısı",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(bars: List<WeeklyPerformanceBar>) {
    if (bars.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Veri bulunmuyor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val maxValue = bars.maxOfOrNull { it.totalCompleted }?.takeIf { it > 0 } ?: 1
    val axisColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(bottom = 16.dp)
            .drawBehind {
                // Y axis
                drawLine(
                    color = axisColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2f,
                )
                // X axis
                drawLine(
                    color = axisColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f,
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            bars.forEach { bar ->
                WeeklyBar(bar = bar, maxValue = maxValue)
            }
        }
    }
}

@Composable
private fun RowScope.WeeklyBar(
    bar: WeeklyPerformanceBar,
    maxValue: Int,
) {
    val fraction = if (maxValue == 0) 0f else bar.totalCompleted / maxValue.toFloat()
    val clampedFraction = fraction.coerceIn(0f, 1f)
    val barColor = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (bar.totalCompleted > 0) {
            Text(
                text = formatCount(bar.totalCompleted),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = barColor,
            )
            Spacer(modifier = Modifier.height(6.dp))
        } else {
            Spacer(modifier = Modifier.height(22.dp))
        }

        val barFraction = when {
            bar.totalCompleted <= 0 -> 0f
            else -> clampedFraction.coerceAtLeast(0.1f)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .fillMaxHeight(barFraction)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(barColor.copy(alpha = 0.9f)),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = bar.dayLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MetricsRow(
    averageDaily: Int,
    bestDaily: Int,
    totalPrayed: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MetricTile(
            label = "Ortalama Hız",
            value = formatCount(averageDaily),
            tint = MaterialTheme.colorScheme.primary,
        )
        MetricTile(
            label = "En İyi Gün",
            value = formatCount(bestDaily),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        MetricTile(
            label = "Toplam Kılınan",
            value = formatCount(totalPrayed),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun RowScope.MetricTile(
    label: String,
    value: String,
    tint: Color,
) {
    ElevatedCard(
        modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = tint,
            )
        }
    }
}

@Composable
private fun DistributionCard(state: StatisticsUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Vakitlere Göre Dağılım",
                style = MaterialTheme.typography.titleMedium,
            )
            val colorScheme = MaterialTheme.colorScheme
            DonutChart(distribution = state.distribution, colorScheme = colorScheme)
            Legend(distribution = state.distribution, colorScheme = colorScheme)
        }
    }
}

@Composable
private fun DonutChart(
    distribution: List<PrayerDistribution>,
    colorScheme: ColorScheme,
) {
    val total = distribution.sumOf { it.completed }.takeIf { it > 0 } ?: return
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(200.dp)) {
            var startAngle = -90f
            distribution.forEach { item ->
                val sweep = (item.completed.toFloat() / total.toFloat()) * 360f
                val color = prayerColor(item.prayerType, colorScheme)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = size.width * 0.12f, cap = StrokeCap.Round),
                )
                startAngle += sweep
            }
        }
    }
}

@Composable
private fun Legend(
    distribution: List<PrayerDistribution>,
    colorScheme: ColorScheme,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        distribution.forEach { item ->
            val color = prayerColor(item.prayerType, colorScheme)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color),
                )
                Text(
                    text = "${item.prayerType.displayName} • ${formatCount(item.completed)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun EmptyStatisticsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Rapora başlamak için kayıt ekleyin",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Bir haftalık ilerleme kaydı oluşturduğunuzda burası dolacak.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun prayerColor(
    prayerType: PrayerType,
    colorScheme: ColorScheme,
): Color = when (prayerType) {
    PrayerType.FAJR -> colorScheme.primary
    PrayerType.DHUHR -> colorScheme.secondary
    PrayerType.ASR -> colorScheme.tertiary
    PrayerType.MAGHRIB -> Color(0xFFDC3545)
    PrayerType.ISHA -> Color(0xFF6C757D)
    PrayerType.WITR -> Color(0xFF17A2B8)
}

private fun formatCount(value: Int): String = NumberFormat.getIntegerInstance(Locale("tr", "TR"))
    .format(value.coerceAtLeast(0))
