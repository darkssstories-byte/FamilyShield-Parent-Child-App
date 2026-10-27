package com.familyshield.parent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import com.familyshield.parent.DemoData

@Composable
fun NotConfiguredScreen(demo: Boolean, onDemo: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Family Shield Parent", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Banner("Firebase is not configured in this build, so sign-in and pairing are disabled.", isError = true)
        ShieldCard {
            Text("To enable the backend", fontWeight = FontWeight.SemiBold)
            Text(
                "1. Create a Firebase project.\n" +
                    "2. Add an Android app with package name com.familyshield.parent.\n" +
                    "3. Download google-services.json and upload it to the app/ folder of your repository.\n" +
                    "4. Re-run the GitHub Actions build and install the new APK.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ShieldCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Preview with DEMO data", modifier = Modifier.weight(1f))
                Switch(checked = demo, onCheckedChange = onDemo)
            }
            Text(
                "Demo data is fictional and is always labelled as such.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (demo) {
            Banner("DEMO DATA — not from a real device")
            DeviceSummary(DemoData.device())
        }
    }
}
