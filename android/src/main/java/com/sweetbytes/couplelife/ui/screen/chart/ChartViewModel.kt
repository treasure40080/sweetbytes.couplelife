package com.sweetbytes.couplelife.ui.screen.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import com.sweetbytes.couplelife.data.local.entity.EntryType
import com.sweetbytes.couplelife.domain.usecase.GetEntriesByMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class PieSlice(
    val label: String,
    val amount: Int,
    val percentage: Float,
    val color: Int
)

data class ChartUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val incomeExpenseData: List<PieSlice> = emptyList(),
    val expenseCategoryData: List<PieSlice> = emptyList(),
    val incomeCategoryData: List<PieSlice> = emptyList(),
    val categoryFilter: EntryType = EntryType.EXPENSE,
    val isEmpty: Boolean = true
) {
    val categoryData: List<PieSlice>
        get() = if (categoryFilter == EntryType.EXPENSE) expenseCategoryData else incomeCategoryData
}

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val getEntriesByMonthUseCase: GetEntriesByMonthUseCase
) : ViewModel() {

    companion object {
        val INCOME_COLOR = 0xFF4CAF50.toInt()
        val EXPENSE_COLOR = 0xFFF44336.toInt()
        val CATEGORY_COLORS = listOf(
            0xFFFF9800, 0xFF9C27B0, 0xFF2196F3, 0xFF009688,
            0xFF607D8B, 0xFFE91E63, 0xFF00BCD4, 0xFF8BC34A
        ).map { it.toInt() }
        val PRESET_CATEGORY_COLORS = mapOf(
            "吃飯" to 0xFFFF9800.toInt(),
            "玩樂" to 0xFF9C27B0.toInt(),
            "薪水" to 0xFF2196F3.toInt(),
            "租屋費" to 0xFF009688.toInt()
        )
    }

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    init {
        loadChart()
    }

    fun onMonthSelected(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month) }
        loadChart()
    }

    fun onCategoryFilterChanged(type: EntryType) {
        _uiState.update { it.copy(categoryFilter = type) }
    }

    private fun loadChart() {
        viewModelScope.launch {
            val y = _uiState.value.selectedYear.toString()
            val m = _uiState.value.selectedMonth.toString().padStart(2, '0')
            getEntriesByMonthUseCase(y, m).collect { entries ->
                _uiState.update {
                    it.copy(
                        incomeExpenseData = calcIncomeExpense(entries),
                        expenseCategoryData = calcCategory(entries.filter { e -> e.type == EntryType.EXPENSE }),
                        incomeCategoryData = calcCategory(entries.filter { e -> e.type == EntryType.INCOME }),
                        isEmpty = entries.isEmpty()
                    )
                }
            }
        }
    }

    private fun calcIncomeExpense(entries: List<EntryEntity>): List<PieSlice> {
        val income = entries.filter { it.type == EntryType.INCOME }.sumOf { it.amount }
        val expense = entries.filter { it.type == EntryType.EXPENSE }.sumOf { it.amount }
        val total = income + expense
        if (total == 0) return emptyList()

        val result = mutableListOf<PieSlice>()
        if (income > 0) {
            result.add(
                PieSlice(
                    label = "收入",
                    amount = income,
                    percentage = (income.toFloat() / total * 100).let { Math.round(it).toFloat() },
                    color = INCOME_COLOR
                )
            )
        }
        if (expense > 0) {
            result.add(
                PieSlice(
                    label = "支出",
                    amount = expense,
                    percentage = (expense.toFloat() / total * 100).let { Math.round(it).toFloat() },
                    color = EXPENSE_COLOR
                )
            )
        }
        return result
    }

    private fun calcCategory(entries: List<EntryEntity>): List<PieSlice> {
        val grouped = entries.groupBy { it.category }
        val total = entries.sumOf { it.amount }
        if (total == 0) return emptyList()

        var colorIndex = 0
        return grouped.map { (category, items) ->
            val amount = items.sumOf { it.amount }
            val color = PRESET_CATEGORY_COLORS[category] ?: run {
                val c = CATEGORY_COLORS[colorIndex % CATEGORY_COLORS.size]
                colorIndex++
                c
            }
            PieSlice(
                label = category,
                amount = amount,
                percentage = (amount.toFloat() / total * 100).let { Math.round(it).toFloat() },
                color = color
            )
        }
    }
}
