package com.sweetbytes.couplelife.ui.screen.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sweetbytes.couplelife.data.local.entity.EntryEntity
import com.sweetbytes.couplelife.data.local.entity.EntryType
import com.sweetbytes.couplelife.domain.usecase.AddEntryUseCase
import com.sweetbytes.couplelife.domain.usecase.DeleteEntryUseCase
import com.sweetbytes.couplelife.domain.usecase.GetEntriesByMonthUseCase
import com.sweetbytes.couplelife.domain.usecase.UpdateEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class EntryUiState(
    val entries: List<EntryEntity> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val isLoading: Boolean = false
)

sealed class EntryEvent {
    object EntryAdded : EntryEvent()
    object EntryUpdated : EntryEvent()
    object EntryDeleted : EntryEvent()
    data class ShowError(val message: String) : EntryEvent()
}

@HiltViewModel
class EntryViewModel @Inject constructor(
    private val addEntryUseCase: AddEntryUseCase,
    private val getEntriesByMonthUseCase: GetEntriesByMonthUseCase,
    private val updateEntryUseCase: UpdateEntryUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryUiState())
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<EntryEvent>()
    val event: SharedFlow<EntryEvent> = _event.asSharedFlow()

    init {
        loadEntries()
    }

    fun onMonthSelected(year: Int, month: Int) {
        _uiState.update { it.copy(selectedYear = year, selectedMonth = month) }
        loadEntries()
    }

    fun addEntry(amount: Int, type: EntryType, category: String) {
        viewModelScope.launch {
            addEntryUseCase(
                EntryEntity(
                    amount = amount,
                    type = type,
                    category = category,
                    createdAt = System.currentTimeMillis()
                )
            )
            _event.emit(EntryEvent.EntryAdded)
        }
    }

    fun updateEntry(entry: EntryEntity) {
        viewModelScope.launch {
            updateEntryUseCase(entry)
            _event.emit(EntryEvent.EntryUpdated)
        }
    }

    fun deleteEntry(entry: EntryEntity) {
        viewModelScope.launch {
            deleteEntryUseCase(entry)
            _event.emit(EntryEvent.EntryDeleted)
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            val y = _uiState.value.selectedYear.toString()
            val m = _uiState.value.selectedMonth.toString().padStart(2, '0')
            getEntriesByMonthUseCase(y, m).collect { list ->
                _uiState.update { it.copy(entries = list) }
            }
        }
    }
}
