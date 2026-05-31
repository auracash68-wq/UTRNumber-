package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admob.AdMobManager
import com.example.data.HistoryExporter
import com.example.data.HistoryItem
import com.example.sound.ClickType
import com.example.sound.SoundManager
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.ThemeColors
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: CalculatorViewModel,
    colors: ThemeColors,
    soundManager: SoundManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val historyList by viewModel.calculationHistory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favoritesOnly by viewModel.onlyFavoritesFilter.collectAsState()
    val selectedIds by viewModel.selectedHistoryIds.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(HistoryExporter.ExportFormat.CSV) }
    var exportResultState by remember { mutableStateOf<HistoryExporter.ExportResult?>(null) }
    
    var showDetailsItem by remember { mutableStateOf<HistoryItem?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // --- 1. SEARCH BAR & FAVORITE FILTER ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search history...", color = colors.numberBtnText.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("history_search_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.displayCardBg.copy(alpha = 0.3f),
                    unfocusedContainerColor = colors.displayCardBg.copy(alpha = 0.15f),
                    focusedTextColor = colors.numberBtnText,
                    unfocusedTextColor = colors.numberBtnText,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.numberBtnText.copy(alpha = 0.6f)
                    )
                },
                singleLine = true
            )

            // Star favorite filter toggle
            IconButton(
                onClick = {
                    soundManager.playClick(ClickType.FUNCTION)
                    viewModel.toggleFavoritesOnly()
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (favoritesOnly) colors.equalsBtnBg else colors.numberBtnBg.copy(alpha = 0.2f))
                    .border(
                        1.dp,
                        if (favoritesOnly) colors.equalsBtnBg else colors.displayCardBorder.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("toggle_favorites_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Filter Favorites",
                    tint = if (favoritesOnly) colors.equalsBtnText else colors.numberBtnText.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. MULTI-DELETE OR ACTION HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedIds.isNotEmpty()) {
                Text(
                    text = "${selectedIds.size} selected",
                    color = colors.numberBtnText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.clearSelections() }) {
                        Text("Cancel", color = colors.numberBtnText.copy(alpha = 0.7f))
                    }
                    Button(
                        onClick = {
                            soundManager.playClick(ClickType.OPERATOR)
                            viewModel.deleteSelectedHistory()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            } else {
                Text(
                    text = "Calculations Ledger",
                    color = colors.numberBtnText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row {
                    // Export Download Button
                    IconButton(onClick = {
                        soundManager.playClick(ClickType.FUNCTION)
                        showExportDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Export History",
                            tint = colors.numberBtnText.copy(alpha = 0.8f)
                        )
                    }

                    // Delete All Button
                    if (historyList.isNotEmpty()) {
                        IconButton(onClick = {
                            soundManager.playClick(ClickType.OPERATOR)
                            showClearConfirm = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 3. SCROLLABLE LEDGER CARDS ---
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = "Empty History",
                        tint = colors.numberBtnText.copy(alpha = 0.25f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No calculations match search query." else "Calculation History is empty.",
                        color = colors.numberBtnText.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Once you evaluate mathematical expressions in radians or degrees, they will appear here.",
                        color = colors.numberBtnText.copy(alpha = 0.35f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("history_items_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    val isChecked = selectedIds.contains(item.id)
                    
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isChecked) colors.displayCardBg.copy(alpha = 0.35f) else colors.displayCardBg.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isChecked) colors.equalsBtnBg else colors.displayCardBorder.copy(alpha = 0.2f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                if (selectedIds.isNotEmpty()) {
                                    viewModel.toggleHistorySelection(item.id)
                                } else {
                                    showDetailsItem = item
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox (Only visible when selection state is active)
                            if (selectedIds.isNotEmpty()) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.toggleHistorySelection(item.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = colors.equalsBtnBg)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Timestamp
                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        color = colors.numberBtnText.copy(alpha = 0.4f),
                                        fontSize = 11.sp
                                    )
                                    // Badge
                                    if (item.isScientific) {
                                        Text(
                                            text = "SCI",
                                            color = colors.functionBtnText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(colors.functionBtnBg.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Expression text
                                Text(
                                    text = item.expression,
                                    color = colors.numberBtnText.copy(alpha = 0.9f),
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Normal
                                )

                                // Result text
                                Text(
                                    text = "= ${item.result}",
                                    color = colors.equalsBtnBg,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Star Action & Delete Action
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Star Favorite Icon
                                IconButton(onClick = {
                                    soundManager.playClick(ClickType.STANDARD)
                                    viewModel.toggleFavoriteHistory(item)
                                }) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite calculation",
                                        tint = if (item.isFavorite) colors.equalsBtnBg else colors.numberBtnText.copy(alpha = 0.3f)
                                    )
                                }

                                // Delete icon
                                IconButton(onClick = {
                                    soundManager.playClick(ClickType.OPERATOR)
                                    viewModel.deleteHistoryItem(item)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete item",
                                        tint = colors.numberBtnText.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. POPUP DETAILED VIEW MODEL DIALOG ---
        if (showDetailsItem != null) {
            val item = showDetailsItem!!
            AlertDialog(
                onDismissRequest = { showDetailsItem = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = colors.displayCardBg.copy(alpha = 0.95f),
                title = { Text("Calculation Log", color = colors.numberBtnText, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Expression", color = colors.numberBtnText.copy(alpha = 0.5f), fontSize = 12.sp)
                            Text(item.expression, color = colors.numberBtnText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        }
                        Column {
                            Text("Result", color = colors.numberBtnText.copy(alpha = 0.5f), fontSize = 12.sp)
                            Text(item.result, color = colors.equalsBtnBg, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Calculated At", color = colors.numberBtnText.copy(alpha = 0.5f), fontSize = 12.sp)
                            Text(dateFormat.format(Date(item.timestamp)), color = colors.numberBtnText, fontSize = 14.sp)
                        }
                        Column {
                            Text("Method Mode", color = colors.numberBtnText.copy(alpha = 0.5f), fontSize = 12.sp)
                            Text(if (item.isScientific) "Scientific Algebra" else "Basic Arithmetic", color = colors.functionBtnText, fontSize = 14.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = colors.equalsBtnBg),
                        onClick = {
                            soundManager.playClick(ClickType.STANDARD)
                            viewModel.pasteExpression(item.expression)
                            showDetailsItem = null
                            Toast.makeText(context, "Loaded into calculator!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Recompute", color = colors.equalsBtnText)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDetailsItem = null }) {
                        Text("Close", color = colors.numberBtnText)
                    }
                },
                modifier = Modifier.border(1.dp, colors.displayCardBorder, RoundedCornerShape(24.dp))
            )
        }

        // --- 5. EXPORT DOWNLOAD CONFIG DIALOG ---
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showExportDialog = false 
                    exportResultState = null
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = colors.displayCardBg.copy(alpha = 0.95f),
                title = { Text("Export History", color = colors.numberBtnText, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Export your entire offline calculation ledger to your local device Downloads folder.",
                            color = colors.numberBtnText.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )

                        Text("Choose Format Type:", color = colors.numberBtnText, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        // Format selection rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryExporter.ExportFormat.values().forEach { format ->
                                val active = selectedFormat == format
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) colors.equalsBtnBg else colors.numberBtnBg.copy(alpha = 0.2f))
                                        .clickable { 
                                            soundManager.playClick(ClickType.STANDARD)
                                            selectedFormat = format 
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = format.name,
                                        color = if (active) colors.equalsBtnText else colors.numberBtnText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Display result outputs
                        if (exportResultState != null) {
                            val res = exportResultState!!
                            Spacer(modifier = Modifier.height(6.dp))
                            when (res) {
                                is HistoryExporter.ExportResult.Success -> {
                                    Column(
                                        modifier = Modifier
                                            .background(colors.functionBtnBg.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text("✓ Export successful!", color = colors.equalsBtnBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Saved inside:", color = colors.numberBtnText, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                        Text(res.filePath, color = colors.numberBtnText.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                is HistoryExporter.ExportResult.Error -> {
                                    Text(
                                        text = "❌ Error: ${res.message}",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (exportResultState is HistoryExporter.ExportResult.Success) {
                        // Success Action, allow Sharing directly
                        val success = exportResultState as HistoryExporter.ExportResult.Success
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = colors.equalsBtnBg),
                            onClick = {
                                soundManager.playClick(ClickType.FUNCTION)
                                HistoryExporter.shareFile(context, success.filePath, success.uri, selectedFormat)
                            }
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Text("Share", color = colors.equalsBtnText)
                            }
                        }
                    } else {
                        // Perform the export
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = colors.equalsBtnBg),
                            onClick = {
                                coroutineScope.launch {
                                    soundManager.playClick(ClickType.EQUALS)
                                    val res = HistoryExporter.export(context, historyList, selectedFormat)
                                    exportResultState = res

                                    if (res is HistoryExporter.ExportResult.Success) {
                                        Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                                        // TRIGGER AdMob Interstitial ad professionally as requested!
                                        val activity = context as? Activity
                                        if (activity != null) {
                                            AdMobManager.showInterstitial(activity) {}
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Confirm Export", color = colors.equalsBtnText)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showExportDialog = false
                        exportResultState = null
                    }) {
                        Text(if (exportResultState is HistoryExporter.ExportResult.Success) "Done" else "Cancel", color = colors.numberBtnText)
                    }
                },
                modifier = Modifier.border(1.dp, colors.displayCardBorder, RoundedCornerShape(24.dp))
            )
        }

        // --- 6. CONFIRM CLEAR ALL DIALOG ---
        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = colors.displayCardBg.copy(alpha = 0.95f),
                title = { Text("Clear All History", color = colors.numberBtnText, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently clear all calculation logs? This action is local and nonrevertible.", color = colors.numberBtnText.copy(alpha = 0.8f)) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            soundManager.playClick(ClickType.OPERATOR)
                            viewModel.deleteAllHistory()
                            showClearConfirm = false
                            Toast.makeText(context, "History cleared completely", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Delete All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel", color = colors.numberBtnText)
                    }
                },
                modifier = Modifier.border(1.dp, colors.displayCardBorder, RoundedCornerShape(24.dp))
            )
        }
    }
}
