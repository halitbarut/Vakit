package com.halitbarut.vakit.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halitbarut.vakit.domain.model.PrayerType
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OnboardingRoute(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is OnboardingEvent.NavigateToDashboard) {
                onCompleted()
            }
        }
    }
    OnboardingScreen(
        state = uiState,
        onYearsChanged = viewModel::updateYears,
        onMonthsChanged = viewModel::updateMonths,
        onDaysChanged = viewModel::updateDays,
        onToggleWitr = viewModel::toggleWitrTracking,
        onToggleHelper = viewModel::toggleHelperSheet,
        onStartTracking = viewModel::startTracking,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onYearsChanged: (com.halitbarut.vakit.domain.model.PrayerType, String) -> Unit,
    onMonthsChanged: (com.halitbarut.vakit.domain.model.PrayerType, String) -> Unit,
    onDaysChanged: (com.halitbarut.vakit.domain.model.PrayerType, String) -> Unit,
    onToggleWitr: (Boolean) -> Unit,
    onToggleHelper: () -> Unit,
    onStartTracking: () -> Unit,
) {
    OnboardingContent(
        state = state,
        onYearsChanged = onYearsChanged,
        onMonthsChanged = onMonthsChanged,
        onDaysChanged = onDaysChanged,
        onToggleWitr = onToggleWitr,
        onToggleHelper = onToggleHelper,
        onStartTracking = onStartTracking,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingContent(
    state: OnboardingUiState,
    onYearsChanged: (PrayerType, String) -> Unit,
    onMonthsChanged: (PrayerType, String) -> Unit,
    onDaysChanged: (PrayerType, String) -> Unit,
    onToggleWitr: (Boolean) -> Unit,
    onToggleHelper: () -> Unit,
    onStartTracking: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val helperSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (state.isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 12.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(
                        text = if (state.isLoading) "Kaydediliyor..." else "Takibi Başlat",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Vakit'e Hoş Geldiniz",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Kaza namazı borcunuzu hesaplayarak yolculuğumuza başlayalım.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                )
                TextButton(onClick = onToggleHelper) {
                    Text(
                        text = "Tam borcunu bilmiyor musun? Yardımcı Hesaplayıcı'yı kullan.",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            val witrInput = state.prayerInputs.firstOrNull { it.prayerType == PrayerType.WITR }
            state.prayerInputs
                .filter { it.prayerType != PrayerType.WITR }
                .forEach { input ->
                    PrayerInputCard(
                        title = input.prayerType.displayName,
                        years = input.years,
                        months = input.months,
                        days = input.days,
                        onYearsChanged = { onYearsChanged(input.prayerType, it) },
                        onMonthsChanged = { onMonthsChanged(input.prayerType, it) },
                        onDaysChanged = { onDaysChanged(input.prayerType, it) },
                    )
                }

            WitrTrackingCard(
                isChecked = state.isWitrTracked,
                onToggle = onToggleWitr,
            )

            if (state.isWitrTracked && witrInput != null) {
                PrayerInputCard(
                    title = witrInput.prayerType.displayName,
                    years = witrInput.years,
                    months = witrInput.months,
                    days = witrInput.days,
                    onYearsChanged = { onYearsChanged(witrInput.prayerType, it) },
                    onMonthsChanged = { onMonthsChanged(witrInput.prayerType, it) },
                    onDaysChanged = { onDaysChanged(witrInput.prayerType, it) },
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    if (state.showHelper) {
        ModalBottomSheet(
            onDismissRequest = onToggleHelper,
            sheetState = helperSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Yardımcı Hesaplayıcı",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Borcunu yaklaşık hesaplamak için aşağıdaki formülü kullanabilirsin:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                HelperTip(
                    title = "1 yıl = 365 vakit",
                    description = "Toplam yılları 365 ile çarp ve ay/gün hesabını ekle.",
                )
                HelperTip(
                    title = "1 ay ≈ 30 vakit",
                    description = "Ay sayısını 30 ile çarpıp toplamına ekle.",
                )
                HelperTip(
                    title = "Gün sayısını ekle",
                    description = "Kalan günleri doğrudan toplayarak nihai sayıyı bul.",
                )
                HelperTip(
                    title = "Vitir",
                    description = "Vitir borcunu takip etmek istemiyorsan ana ekrandan her an kapatabilirsin.",
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PrayerInputCard(
    title: String,
    years: String,
    months: String,
    days: String,
    onYearsChanged: (String) -> Unit,
    onMonthsChanged: (String) -> Unit,
    onDaysChanged: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OnboardingNumberField(
                    label = "Yıl",
                    value = years,
                    onValueChange = onYearsChanged,
                )
                OnboardingNumberField(
                    label = "Ay",
                    value = months,
                    onValueChange = onMonthsChanged,
                )
                OnboardingNumberField(
                    label = "Gün",
                    value = days,
                    onValueChange = onDaysChanged,
                )
            }
        }
    }
}

@Composable
private fun RowScope.OnboardingNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.weight(1f),
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun WitrTrackingCard(
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Vitir",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Vitir namazı borcunuzu takip etmek istiyorsanız anahtar açık kalsın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun HelperTip(
    title: String,
    description: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}
