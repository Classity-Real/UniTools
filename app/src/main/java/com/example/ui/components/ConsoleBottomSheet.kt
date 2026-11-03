package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.executor.LogEntry
import com.example.ui.theme.StatusGreen

@Composable
fun ConsoleBottomSheet(
    logs: List<LogEntry>,
    isExecuting: Boolean,
    currentTask: String,
    isExpanded: Boolean,
    onToggleExpand: (Boolean) -> Unit,
    onClearLogs: () -> Unit,
    onCancelTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new log entry
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    val heightAnimation by animateDpAsState(
        targetValue = if (isExpanded) 360.dp else 58.dp,
        label = "console_height"
    )

    // Dynamic Material You Palette Colors matching Navigation Bar
    val navBarColor = MaterialTheme.colorScheme.surface
    val containerBg = navBarColor
    val headerBg = navBarColor
    val textColor = MaterialTheme.colorScheme.onSurface
    val cmdColor = MaterialTheme.colorScheme.primary
    val errColor = MaterialTheme.colorScheme.error

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(heightAnimation)
            .testTag("console_bottom_sheet"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = containerBg,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .background(headerBg)
                    .clickable { onToggleExpand(!isExpanded) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = cmdColor,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Console",
                            tint = cmdColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isExecuting) "RUNNING: $currentTask" else "Console Log (${logs.size})",
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!isExpanded && logs.isNotEmpty()) {
                            Text(
                                text = logs.last().message,
                                color = textColor.copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Header action buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isExecuting) {
                        IconButton(
                            onClick = onCancelTask,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Abort process",
                                tint = errColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    if (isExpanded) {
                        IconButton(
                            onClick = {
                                val fullLog = logs.joinToString("\n") { "[${it.timestamp}] ${it.message}" }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("UniTools Log", fullLog))
                                Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy log",
                                tint = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onClearLogs,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear log",
                                tint = textColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onToggleExpand(!isExpanded) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = if (isExpanded) "Collapse console" else "Expand console",
                            tint = cmdColor
                        )
                    }
                }
            }

            // Expanded terminal body with theme-matched colors
            AnimatedVisibility(visible = isExpanded) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(containerBg)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (logs.isEmpty()) {
                        item {
                            Text(
                                text = "UniTools console initialized. Ready for operations.",
                                color = textColor.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        items(logs) { entry ->
                            val entryColor = when {
                                entry.isError -> errColor
                                entry.isCommand -> cmdColor
                                entry.message.contains("[FINISHED]", ignoreCase = true) -> StatusGreen
                                entry.message.contains("[FAILED]", ignoreCase = true) -> errColor
                                entry.message.contains("[DIAGNOSTIC]", ignoreCase = true) -> MaterialTheme.colorScheme.tertiary
                                else -> textColor
                            }
                            Text(
                                text = "[${entry.timestamp}] ${entry.message}",
                                color = entryColor,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
