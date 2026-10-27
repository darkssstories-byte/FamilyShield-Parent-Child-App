package com.familyshield.parent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyshield.parent.ChildDevice
import com.familyshield.parent.UiState
import com.familyshield.parent.formatAgo
import com.familyshield.parent.formatMinutes

@Composable
fun DashboardScreen(ui: UiState, onPair: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val devices = ui.visibleDevices

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (ui.demo) Banner("DEMO DATA — not from a real device")
        ui.devicesError?.let { Banner(it, isError = true) }
        when {
            ui.devicesLoading && devices.isEmpty() -> Box(
                Modifier.fillMaxWidth().padding(40.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            devices.isEmpty() -> EmptyState(
                title = "No paired devices yet",
                body = "Pair your child's phone to see battery and screen-time information here.",
                actionLabel = "Pair a device",
                onAction = onPair
            )

            else -> {
                val device = devices.firstOrNull { it.id == selected } ?: devices.first()
                if (devices.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        devices.forEach { d ->
                            FilterChip(
                                selected = d.id == device.id,
                                onClick = { selected = d.id },
                                label = { Text(d.name) }
                            )
                        }
                    }
                }
                DeviceSummary(device)
            }
        }
    }
}

@Composable
fun DeviceSummary(device: ChildDevice) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ShieldCard {
            Text(device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Last sync: " + formatAgo(device.lastSyncMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ShieldCard {
            val pct = device.batteryPercent
            Text("Battery", style = MaterialTheme.typography.titleSmall)
            Text(
                if (pct == null) "Unknown" else "$pct%" + if (device.charging) " · charging" else "",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (pct != null && pct <= 20 && !device.charging) Danger else MaterialTheme.colorScheme.onSurface
            )
            if (pct != null) {
                LinearProgressIndicator(
                    progress = { pct / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (pct <= 20 && !device.charging) Danger else Purple
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShieldCard(Modifier.weight(1f)) {
                Text("Today", style = MaterialTheme.typography.titleSmall)
                Text(
                    formatMinutes(device.screenTimeTodayMinutes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            ShieldCard(Modifier.weight(1f)) {
                Text("This week", style = MaterialTheme.typography.titleSmall)
                Text(
                    formatMinutes(device.screenTimeWeekMinutes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        ShieldCard {
            Text("Most used apps", style = MaterialTheme.typography.titleSmall)
            if (device.topApps.isEmpty()) {
                Text(
                    "No app usage reported yet. The child app must be installed and Usage Access enabled.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val max = device.topApps.maxOf { it.minutes }.coerceAtLeast(1)
                device.topApps.forEach { app ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(app.name, modifier = Modifier.weight(1f))
                        Text(formatMinutes(app.minutes), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LinearProgressIndicator(
                        progress = { app.minutes / max.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = Purple
                    )
                }
            }
        }
    }
}
