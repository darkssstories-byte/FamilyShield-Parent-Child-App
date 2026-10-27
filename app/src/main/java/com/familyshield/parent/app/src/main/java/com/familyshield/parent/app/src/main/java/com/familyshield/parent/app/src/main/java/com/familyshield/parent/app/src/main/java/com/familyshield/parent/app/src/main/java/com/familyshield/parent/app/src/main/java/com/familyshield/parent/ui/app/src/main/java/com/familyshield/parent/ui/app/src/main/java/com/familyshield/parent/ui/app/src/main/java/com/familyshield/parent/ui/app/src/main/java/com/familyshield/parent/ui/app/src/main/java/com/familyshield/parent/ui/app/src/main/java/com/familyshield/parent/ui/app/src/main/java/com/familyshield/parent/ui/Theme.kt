package com.familyshield.parent.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF0B1020)
val NavyCard = Color(0xFF151C33)
val Purple = Color(0xFF8B5CF6)
val PurpleLight = Color(0xFFC4B5FD)
val TextPrimary = Color(0xFFEDEBFF)
val TextMuted = Color(0xFFA5A9C4)
val Danger = Color(0xFFFF6B7A)

@Composable
fun FamilyShieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            onPrimary = Color.White,
            secondary = PurpleLight,
            background = Navy,
            onBackground = TextPrimary,
            surface = NavyCard,
            onSurface = TextPrimary,
            surfaceVariant = Color(0xFF1D2644),
            onSurfaceVariant = TextMuted,
            error = Danger
        ),
        content = content
    )
}
