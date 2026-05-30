package com.woodworking.calculatorpro.ui.screens

import androidx.compose.material3.SnackbarHostState
import com.woodworking.calculatorpro.WoodworkingApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Shared "save current calculation to history" helper. Posts a snackbar with
 * the localised "Saved" message on success.
 */
fun saveToHistory(
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
    toolKey: String,
    title: String,
    summary: String,
    savedMessage: String,
) {
    scope.launch(Dispatchers.IO) {
        WoodworkingApp.get().historyRepository.save(toolKey, title, summary)
    }
    scope.launch { snackbar.showSnackbar(savedMessage) }
}
