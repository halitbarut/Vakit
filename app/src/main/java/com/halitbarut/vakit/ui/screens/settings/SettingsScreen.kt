package com.halitbarut.vakit.ui.screens.settings

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halitbarut.vakit.R
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.text.Charsets

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { }
    LaunchedEffect(viewModel, context) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ShareCsv -> {
                    try {
                        sharePrayerCsv(context, event.content, shareLauncher)
                    } catch (error: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.export_data_error),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

                SettingsEvent.ExportFailed -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.export_data_error),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = uiState,
        onBack = onBack,
        onToggleWitr = viewModel::onToggleWitr,
        onToggleNotifications = viewModel::onToggleNotifications,
        onNotificationTimeSelected = viewModel::onNotificationTimeSelected,
        onExportData = viewModel::onExportDataClicked,
        onReset = viewModel::onResetClicked,
        onResetConfirm = viewModel::onResetAllConfirmed,
        onDismissDialog = viewModel::dismissDialog,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onToggleWitr: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onNotificationTimeSelected: (Int, Int) -> Unit,
    onExportData: () -> Unit,
    onReset: () -> Unit,
    onResetConfirm: () -> Unit,
    onDismissDialog: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    Scaffold(
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Outlined.ArrowBackIosNew, contentDescription = "Geri")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SettingsSection(title = "Genel") {
                SettingToggleRow(
                    title = "Vitir Namazı Takibi",
                    description = "Vitir borcunu da takip etmek için açık tut.",
                    checked = state.isWitrTracked,
                    onCheckedChange = onToggleWitr,
                )
                SettingToggleRow(
                    title = "Bildirimler",
                    description = "Günlük hatırlatma bildirimlerini al.",
                    checked = state.notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                )
                if (state.notificationsEnabled) {
                    ReminderTimeRow(
                        time = state.notificationTime,
                        onClick = {
                            val (hour, minute) = parseTime(state.notificationTime)
                            TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    onNotificationTimeSelected(selectedHour, selectedMinute)
                                },
                                hour,
                                minute,
                                true,
                            ).show()
                        },
                    )
                }
            }

            SettingsSection(title = "Veri Yönetimi") {
                SettingActionRow(
                    title = "Verileri Dışa Aktar",
                    description = "CSV olarak paylaş.",
                    enabled = !state.isExporting,
                    trailingIcon = {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(imageVector = Icons.Outlined.ArrowForwardIos, contentDescription = null)
                        }
                    },
                    onClick = onExportData,
                )
                SettingActionRow(
                    title = "Tüm Verileri Sıfırla",
                    description = "Dikkat: Geri dönüş yok.",
                    titleColor = MaterialTheme.colorScheme.error,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onClick = onReset,
                )
            }

            SettingsSection(title = "Hakkında") {
                SettingActionRow(
                    title = "Uygulamayı Değerlendir",
                    trailingIcon = {
                        Icon(imageVector = Icons.Outlined.ArrowForwardIos, contentDescription = null)
                    },
                    onClick = {},
                )
                SettingActionRow(
                    title = "Gizlilik Politikası",
                    trailingIcon = {
                        Icon(imageVector = Icons.Outlined.ArrowForwardIos, contentDescription = null)
                    },
                    onClick = {},
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Vakit v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (state.showResetConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text("Tüm verileri sıfırlamak istiyor musun?") },
            text = { Text("Bu işlem tüm ilerlemeni siler ve seni tekrar başlangıca yönlendirir.") },
            confirmButton = {
                Button(onClick = onResetConfirm) {
                    Text("Evet, sıfırla")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text("Vazgeç")
                }
            },
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(Locale("tr", "TR")),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ElevatedCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    description: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (trailingIcon != null) {
                trailingIcon()
            }
        }
    }
}

@Composable
private fun ReminderTimeRow(
    time: String,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Hatırlatma Saati",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Günlük bildirimin gönderileceği saat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = null,
                )
            }
        }
    }
}

private suspend fun sharePrayerCsv(
    context: Context,
    csv: String,
    launcher: ActivityResultLauncher<Intent>,
) {
    val exportFile = withContext(Dispatchers.IO) {
        val exportsDir = File(context.cacheDir, "exports").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        File(exportsDir, "prayer_data.csv").apply {
            writeText(csv, Charsets.UTF_8)
        }
    }

    val fileUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        exportFile,
    )
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newRawUri("Prayer data", fileUri)
    }
    val chooserIntent = Intent.createChooser(
        sendIntent,
        context.getString(R.string.share_prayer_data_title),
    )
    launcher.launch(chooserIntent)
}

private fun parseTime(time: String): Pair<Int, Int> {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 30
    return hour to minute
}
