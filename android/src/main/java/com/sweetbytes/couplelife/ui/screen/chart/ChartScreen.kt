package com.sweetbytes.couplelife.ui.screen.chart

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import com.sweetbytes.couplelife.data.local.entity.EntryType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sweetbytes.couplelife.ui.screen.entry.MonthPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(viewModel: ChartViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMonthPicker by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("統計圖表") },
                actions = {
                    TextButton(onClick = { showMonthPicker = true }) {
                        Text("${uiState.selectedYear}年 ${uiState.selectedMonth.toString().padStart(2, '0')}月")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (uiState.isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("本月尚無記帳資料")
                }
            } else {
                SectionTitle("收入 vs 支出")
                PieChartView(
                    slices = uiState.incomeExpenseData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                LegendSection(uiState.incomeExpenseData)

                Spacer(modifier = Modifier.height(16.dp))

                SectionTitle("各分類佔比")
                TabRow(
                    selectedTabIndex = if (uiState.categoryFilter == EntryType.EXPENSE) 0 else 1
                ) {
                    Tab(
                        selected = uiState.categoryFilter == EntryType.EXPENSE,
                        onClick = { viewModel.onCategoryFilterChanged(EntryType.EXPENSE) },
                        text = { Text("支出") }
                    )
                    Tab(
                        selected = uiState.categoryFilter == EntryType.INCOME,
                        onClick = { viewModel.onCategoryFilterChanged(EntryType.INCOME) },
                        text = { Text("收入") }
                    )
                }
                if (uiState.categoryData.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.categoryFilter == EntryType.EXPENSE) "本月無支出記帳" else "本月無收入記帳",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    PieChartView(
                        slices = uiState.categoryData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    LegendSection(uiState.categoryData)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun LegendSection(slices: List<PieSlice>) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        slices.forEach { slice ->
            LegendRow(slice)
        }
    }
}

@Composable
private fun LegendRow(slice: PieSlice) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(Color(slice.color))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = slice.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${"%.0f".format(slice.percentage)}% / ${slice.amount}元",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
