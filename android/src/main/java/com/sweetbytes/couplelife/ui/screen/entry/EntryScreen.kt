package com.sweetbytes.couplelife.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import com.sweetbytes.couplelife.data.local.entity.EntryType
import com.sweetbytes.couplelife.ui.screen.entry.AddEntryDialog
import com.sweetbytes.couplelife.ui.screen.entry.EditEntryDialog
import com.sweetbytes.couplelife.ui.screen.entry.EntryEvent
import com.sweetbytes.couplelife.ui.screen.entry.EntryViewModel
import com.sweetbytes.couplelife.ui.screen.entry.MonthPickerDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    viewModel: EntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<EntryEntity?>(null) }
    var deletingEntry by remember { mutableStateOf<EntryEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is EntryEvent.EntryAdded -> showAddDialog = false
                is EntryEvent.EntryUpdated -> editingEntry = null
                is EntryEvent.EntryDeleted -> deletingEntry = null
                is EntryEvent.ShowError -> { /* TODO: show snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("記帳流水") },
                actions = {
                    TextButton(onClick = { showMonthPicker = true }) {
                        Text(
                            "${uiState.selectedYear} / ${uiState.selectedMonth.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "新增記帳")
            }
        }
    ) { innerPadding ->
        if (uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "本月尚無記帳紀錄\n點擊右下角 + 新增",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.entries) { entry ->
                    EntryItem(
                        entry = entry,
                        onClick = { editingEntry = entry },
                        onDelete = { deletingEntry = entry }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            onConfirm = { amount, type, category ->
                viewModel.addEntry(amount, type, category)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onConfirm = { updated -> viewModel.updateEntry(updated) },
            onDismiss = { editingEntry = null }
        )
    }

    deletingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            title = { Text("確認刪除") },
            text = { Text("確定要刪除這筆記帳嗎？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEntry(entry); deletingEntry = null }) {
                    Text("確定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntry = null }) { Text("取消") }
            }
        )
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            currentYear = uiState.selectedYear,
            currentMonth = uiState.selectedMonth,
            onConfirm = { year, month ->
                viewModel.onMonthSelected(year, month)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }
}

@Composable
private fun EntryItem(entry: EntryEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    val isIncome = entry.type == EntryType.INCOME
    val amountColor = if (isIncome) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    val amountPrefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = entry.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateFormat.format(Date(entry.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amountPrefix${entry.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "刪除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
