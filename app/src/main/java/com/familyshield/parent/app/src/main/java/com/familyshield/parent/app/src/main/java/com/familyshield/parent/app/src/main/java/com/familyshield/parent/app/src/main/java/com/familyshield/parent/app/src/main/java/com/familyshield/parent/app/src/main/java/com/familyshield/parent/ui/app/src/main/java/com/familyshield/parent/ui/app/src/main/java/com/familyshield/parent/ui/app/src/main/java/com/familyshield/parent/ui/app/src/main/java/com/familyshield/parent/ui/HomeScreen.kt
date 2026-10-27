package com.familyshield.parent.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.familyshield.parent.UiState

private data class Tab(val label: String, val icon: ImageVector)

@Composable
fun HomeScreen(
    ui: UiState,
    onGenerateCode: () -> Unit,
    onUnlink: (String) -> Unit,
    onDemo: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    onDeleteData: () -> Unit,
    onMessageShown: () -> Unit
) {
    val tabs = listOf(
        Tab("Dashboard", Icons.Filled.Home),
        Tab("Devices", Icons.Filled.Phone),
        Tab("Settings", Icons.Filled.Settings)
    )
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.message) {
        ui.message?.let {
            snackbar.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        val mod = Modifier.padding(padding)
        androidx.compose.foundation.layout.Box(mod) {
            when (tab) {
                0 -> DashboardScreen(ui, onPair = { tab = 1 })
                1 -> DevicesScreen(ui, onGenerateCode, onUnlink)
                else -> SettingsScreen(ui, onDemo, onSignOut, onDeleteData)
            }
        }
    }
}
