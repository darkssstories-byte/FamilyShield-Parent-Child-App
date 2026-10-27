package com.familyshield.parent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.familyshield.parent.PairingCode
import com.familyshield.parent.PairingUi
import com.familyshield.parent.UiState
import com.familyshield.parent.formatAgo
import kotlinx.coroutines.delay

@Composable
fun DevicesScreen(ui: UiState, onGenerateCode: () -> Unit, onUnlink: (String) -> Unit) {
    var unlinkTarget by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Devices", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (ui.demo) Banner("DEMO DATA — not from a real device")

        ShieldCard {
            Text("Pair a child device", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Generate a one-time code, then enter it in the Family Shield Child app on your child's phone. " +
                    "Codes are short-lived and can be used once.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when (val p = ui.pairing) {
                is PairingUi.Idle -> Unit
                is PairingUi.Loading -> CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
                is PairingUi.Error -> Banner(p.message, isError = true)
                is PairingUi.Ready -> CodeDisplay(p.code)
            }
            Button(
                onClick = onGenerateCode,
                enabled = ui.pairing !is PairingUi.Loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Generate pairing code") }
        }

        Text("Paired devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        val devices = ui.visibleDevices
        if (devices.isEmpty()) {
            EmptyState("No paired devices", "Devices you pair will appear here with their connection status.")
        } else {
            devices.forEach { d ->
                ShieldCard {
                    Text(d.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Last sync: " + formatAgo(d.lastSyncMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (d.isDemo) {
                        Text("Sample data — cannot be unlinked", style = MaterialTheme.typography.bodySmall)
                    } else {
                        OutlinedButton(onClick = { unlinkTarget = d.id }) { Text("Unlink device") }
                    }
                }
            }
        }
    }

    unlinkTarget?.let { id ->
        AlertDialog(
            onDismissRequest = { unlinkTarget = null },
            title = { Text("Unlink this device?") },
            text = { Text("The child device will stop syncing and its data link will be removed.") },
            confirmButton = {
                TextButton(onClick = { onUnlink(id); unlinkTarget = null }) { Text("Unlink") }
            },
            dismissButton = {
                TextButton(onClick = { unlinkTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CodeDisplay(code: PairingCode) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(code) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val remaining = ((code.expiresAtMillis - now) / 1000).coerceAtLeast(0)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (remaining > 0) {
            Text(
                code.code.chunked(3).joinToString(" "),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PurpleLight,
                textAlign = TextAlign.Center
            )
            Text(
                "Expires in ${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text("Code expired. Generate a new one.", color = Danger)
        }
    }
}
