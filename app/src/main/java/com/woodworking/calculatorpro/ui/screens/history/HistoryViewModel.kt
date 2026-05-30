package com.woodworking.calculatorpro.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woodworking.calculatorpro.WoodworkingApp
import com.woodworking.calculatorpro.data.HistoryEntity
import com.woodworking.calculatorpro.data.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    // No-arg ctor so the default `viewModel()` factory can instantiate via
    // reflection. We resolve the repository lazily from the Application.
    private val repo: HistoryRepository = WoodworkingApp.get().historyRepository

    val items: StateFlow<List<HistoryEntity>> =
        repo.observeAll().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) { repo.delete(id) }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) { repo.clear() }
    }
}
