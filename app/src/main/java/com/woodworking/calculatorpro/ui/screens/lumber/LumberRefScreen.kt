package com.woodworking.calculatorpro.ui.screens.lumber

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.domain.LumberData
import com.woodworking.calculatorpro.ui.components.WCard
import com.woodworking.calculatorpro.ui.components.WResultDivider
import com.woodworking.calculatorpro.ui.components.WScreenScaffold
import com.woodworking.calculatorpro.ui.components.WSegmented

private enum class LumberCategory { SOFTWOOD, SHEET }

@Composable
fun LumberRefScreen(onBack: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    var category by remember { mutableStateOf(LumberCategory.SOFTWOOD) }

    val items = when (category) {
        LumberCategory.SOFTWOOD -> LumberData.softwood
        LumberCategory.SHEET -> LumberData.sheetGoods
    }

    WScreenScaffold(
        title = stringResource(R.string.tool_lumber),
        onBack = onBack,
        snackbarHostState = snackbar,
    ) {
        WCard(title = stringResource(R.string.tool_lumber_desc)) {
            WSegmented(
                options = listOf(LumberCategory.SOFTWOOD, LumberCategory.SHEET),
                selected = category,
                onSelected = { category = it },
                label = {
                    when (it) {
                        LumberCategory.SOFTWOOD -> stringResource(R.string.lumber_softwood)
                        LumberCategory.SHEET -> stringResource(R.string.lumber_sheet)
                    }
                },
            )
        }

        WCard(title = null) {
            // Header row.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeaderCell(stringResource(R.string.lumber_nominal), Modifier.weight(1f))
                HeaderCell(stringResource(R.string.lumber_actual_in), Modifier.weight(1.4f))
                HeaderCell(stringResource(R.string.lumber_actual_mm), Modifier.weight(1.0f))
            }
            WResultDivider()
            items.forEachIndexed { idx, size ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Cell(size.nominal, Modifier.weight(1f), bold = true)
                    Cell(size.actualInches, Modifier.weight(1.4f))
                    Cell(size.actualMm, Modifier.weight(1.0f))
                }
                if (size.notes.isNotEmpty()) {
                    Text(
                        text = size.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                    )
                }
                if (idx != items.lastIndex) WResultDivider()
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(vertical = 4.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun Cell(text: String, modifier: Modifier = Modifier, bold: Boolean = false) {
    Box(modifier = modifier) {
        Text(
            text = text,
            style = if (bold) MaterialTheme.typography.titleSmall
                    else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
