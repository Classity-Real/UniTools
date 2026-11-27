package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppTheme
import com.example.ui.theme.InstrumentAccent
import com.example.ui.theme.InstrumentBackground
import com.example.ui.theme.InstrumentBorder
import com.example.ui.theme.InstrumentForeground
import com.example.ui.theme.InstrumentHeading
import com.example.ui.theme.InstrumentMuted
import com.example.ui.theme.InstrumentSurface

private data class ThemeSwatch(
    val base: Color,
    val surface: Color,
    val accent: Color
)

private fun AppTheme.swatch(): ThemeSwatch = when (this) {
    AppTheme.LIQUID_GLASS -> ThemeSwatch(InstrumentBackground, InstrumentSurface, InstrumentAccent)
    AppTheme.MATERIAL -> ThemeSwatch(Color(0xFFF4FBFA), Color(0xFFE0F2F1), Color(0xFF00695C))
    AppTheme.M3_EXPRESSIVE -> ThemeSwatch(Color(0xFF101418), Color(0xFF1B2A28), Color(0xFF2BD4A8))
}

/**
 * Settings screen — appearance only. Theme selection behaves like firmware
 * configuration: dense, grouped, monospaced labels, live swatches, explicit state.
 */
@Composable
fun ThemeSettingsScreen(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val isInstrument = currentTheme == AppTheme.LIQUID_GLASS

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "UNITOOLS / SETTINGS / APPEARANCE",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                color = if (isInstrument) InstrumentMuted
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(Modifier.height(6.dp))
        Text(
            text = "Theme Engine",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isInstrument) InstrumentHeading
                else MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Select how UniTools renders across the console, file manager and flash " +
                "operations. The active theme applies immediately; no restart required.",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp,
                color = if (isInstrument) InstrumentForeground
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(Modifier.height(20.dp))

        AppTheme.entries.forEachIndexed { index, theme ->
            ThemeOptionRow(
                theme = theme,
                selected = theme == currentTheme,
                index = index,
                isInstrument = isInstrument,
                onClick = { onThemeSelected(theme) }
            )
            if (index != AppTheme.entries.lastIndex) {
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(28.dp))

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isInstrument) InstrumentSurface else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "//",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = if (isInstrument) InstrumentMuted
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "ACTIVE: ${currentTheme.name}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = if (isInstrument) InstrumentAccent
                        else MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ThemeOptionRow(
    theme: AppTheme,
    selected: Boolean,
    index: Int,
    isInstrument: Boolean,
    onClick: () -> Unit
) {
    val swatch = theme.swatch()
    val shape = RoundedCornerShape(6.dp)
    val rowBg by animateColorAsState(
        targetValue = when {
            selected && isInstrument -> InstrumentSurface
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            isInstrument -> InstrumentSurface.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "theme_row_bg"
    )
    val rowBorder by animateColorAsState(
        targetValue = when {
            selected -> if (isInstrument) InstrumentAccent else MaterialTheme.colorScheme.primary
            isInstrument -> InstrumentBorder
            else -> MaterialTheme.colorScheme.outlineVariant
        },
        label = "theme_row_border"
    )
    val titleColor = if (isInstrument) InstrumentHeading else MaterialTheme.colorScheme.onSurface
    val descColor = if (isInstrument) InstrumentMuted else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(rowBg)
            .border(1.dp, rowBorder, shape)
            .clickable(onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Live swatch preview
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(shape)
                .background(swatch.base)
                .border(1.dp, InstrumentBorder, shape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(swatch.surface)
            )
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(swatch.accent)
                    .align(Alignment.BottomCenter)
            )
        }

        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%02d".format(index + 1),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = if (isInstrument) InstrumentMuted else descColor
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = theme.label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 16.sp,
                    color = descColor
                )
            )
        }

        Spacer(Modifier.width(10.dp))

        // Explicit state
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) rowBorder else descColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(rowBorder, CircleShape)
                )
            }
        }
    }
}
