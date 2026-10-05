package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalculationEntity
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CalculatorViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CalculatorViewModel,
    theme: ThemeMode,
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.historyList.collectAsState()
    val haptic = LocalHapticFeedback.current

    var searchQuery by remember { mutableStateOf("") }
    var filterFavoritesOnly by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedItemForSheet by remember { mutableStateOf<CalculationEntity?>(null) }

    val filteredList = remember(historyList, searchQuery, filterFavoritesOnly) {
        historyList.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.expression.contains(searchQuery, ignoreCase = true) ||
                    item.result.contains(searchQuery, ignoreCase = true)
            val matchesFav = !filterFavoritesOnly || item.isFavorite
            matchesSearch && matchesFav
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        // ----------------------------------------------------
        // Top Header: Title "History" & Favorites / Clear Actions
        // ----------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "History",
                    color = theme.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredList.size} calculations",
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        filterFavoritesOnly = !filterFavoritesOnly
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (theme.isLight) Color.White else Color(0x33FFFFFF))
                        .border(1.dp, if (theme.isLight) Color(0x0F000000) else Color(0x22FFFFFF), CircleShape)
                        .testTag("filter_fav_btn")
                ) {
                    Icon(
                        imageVector = if (filterFavoritesOnly) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorites",
                        tint = if (filterFavoritesOnly) Color(0xFFFFB703) else theme.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (historyList.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showClearDialog = true
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (theme.isLight) Color.White else Color(0x33FFFFFF))
                            .border(1.dp, if (theme.isLight) Color(0x0F000000) else Color(0x22FFFFFF), CircleShape)
                            .testTag("clear_all_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ----------------------------------------------------
        // Search Bar (Lightweight & Smooth)
        // ----------------------------------------------------
        LiquidGlassCard(
            theme = theme,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = theme.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search calculations...",
                            color = theme.textSecondary.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_input")
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ----------------------------------------------------
        // History List
        // ----------------------------------------------------
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = theme.textSecondary.copy(alpha = 0.35f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty() || filterFavoritesOnly) "No calculations match" else "No history yet",
                        color = theme.textSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        theme = theme,
                        onTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.reuseHistory(item, asExpression = false)
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedItemForSheet = item
                        },
                        onToggleFavorite = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleFavoriteHistory(item)
                        }
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // Long Press Bottom Sheet:
    // - Copy Expression
    // - Copy Result
    // - Reuse in Calculator
    // - Delete
    // ----------------------------------------------------
    if (selectedItemForSheet != null) {
        val targetItem = selectedItemForSheet!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { selectedItemForSheet = null },
            sheetState = sheetState,
            containerColor = if (theme.isLight) Color(0xFFFBF9F6) else Color(0xFF1E1E22),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (theme.isLight) Color.White else Color(0x22FFFFFF))
                        .padding(14.dp)
                ) {
                    Text(text = targetItem.expression, color = theme.textSecondary, fontSize = 14.sp)
                    Text(text = "= ${targetItem.result}", color = theme.primaryAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Copy Expression
                BottomSheetActionRow(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Expression",
                    theme = theme
                ) {
                    viewModel.copyToClipboard(targetItem.expression)
                    selectedItemForSheet = null
                }

                // Copy Result
                BottomSheetActionRow(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy Result",
                    theme = theme
                ) {
                    viewModel.copyToClipboard(targetItem.result)
                    selectedItemForSheet = null
                }

                // Reuse in Calculator
                BottomSheetActionRow(
                    icon = Icons.Default.Calculate,
                    label = "Reuse in Calculator",
                    theme = theme
                ) {
                    viewModel.reuseHistory(targetItem, asExpression = false)
                    selectedItemForSheet = null
                }

                // Delete
                BottomSheetActionRow(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    theme = theme,
                    tint = Color(0xFFFF5252)
                ) {
                    viewModel.deleteHistoryItem(targetItem)
                    selectedItemForSheet = null
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Confirmation Dialog for Clearing All History
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(text = "Clear All History", color = theme.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all stored calculations?",
                    color = theme.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Clear All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = theme.textSecondary)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = if (theme.isLight) Color.White else Color(0xFF242428),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ----------------------------------------------------
// Optimized History Item Card (Zero lag on low-end phones)
// ----------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryItemCard(
    item: CalculationEntity,
    theme: ThemeMode,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val cardBg = if (theme.isLight) Color.White else theme.surfaceGlass
    val cardBorder = if (theme.isLight) Color(0x0F000000) else theme.borderGlass.copy(alpha = 0.35f)
    val formattedDate = remember(item.timestamp) { formatHistoryTimestamp(item.timestamp) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("history_item_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamp
                Text(
                    text = formattedDate,
                    color = theme.textSecondary.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) Color(0xFFFFB703) else theme.textSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Expression
            Text(
                text = item.expression,
                color = theme.textSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Result
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.result,
                    color = theme.primaryAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Hold for options",
                    color = theme.textSecondary.copy(alpha = 0.45f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// Safe Relative Date Formatter Utility (Crash-Free)
// ----------------------------------------------------
private fun formatHistoryTimestamp(timestamp: Long): String {
    return try {
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calItem = Calendar.getInstance().apply { timeInMillis = timestamp }

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))

        val isSameDay = calNow.get(Calendar.YEAR) == calItem.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calItem.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            return "Today, $timeFormat"
        }

        calNow.add(Calendar.DAY_OF_YEAR, -1)
        val isYesterday = calNow.get(Calendar.YEAR) == calItem.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calItem.get(Calendar.DAY_OF_YEAR)

        if (isYesterday) {
            return "Yesterday, $timeFormat"
        }

        val isSameYear = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR) == calItem.get(Calendar.YEAR)
        val dateFormat = if (isSameYear) {
            SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
        } else {
            SimpleDateFormat("MMM d, yyyy, hh:mm a", Locale.getDefault())
        }
        dateFormat.format(Date(timestamp))
    } catch (e: Exception) {
        "Recent"
    }
}

@Composable
private fun BottomSheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    theme: ThemeMode,
    tint: Color = theme.textPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = label, color = tint, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
