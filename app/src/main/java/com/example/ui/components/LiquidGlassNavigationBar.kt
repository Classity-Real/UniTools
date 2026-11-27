package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Miuix-style liquid glass navigation bar: a floating translucent pill with a
 * blurred color wash behind it, a soft top highlight and a hairline border.
 * Icons sit on "lenses" that glow when selected.
 */
@Composable
fun LiquidGlassNavigationBar(
    items: List<NavigationItem>,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    val glassBase = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val accent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Blurred accent wash behind the glass
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 24.dp)
                .clip(shape)
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.35f), accent.copy(alpha = 0.05f))
                    )
                )
                .blur(18.dp)
        )

        Row(
            modifier = Modifier
                .clip(shape)
                .background(glassBase)
                .border(1.dp, borderColor, shape)
                .drawBehind {
                    // top glass highlight
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height / 2
                        )
                    )
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = item.selected
                val lensColor by animateColorAsState(
                    targetValue = if (selected) accent.copy(alpha = 0.22f) else Color.Transparent,
                    label = "glass_lens_color"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "glass_content_color"
                )
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.06f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 380f),
                    label = "glass_item_scale"
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clip(RoundedCornerShape(20.dp))
                        .background(lensColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { item.onClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        if (selected) {
                            Text(
                                text = item.title,
                                color = contentColor,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
