package com.woodworking.calculatorpro.ui.screens.boardcut

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.domain.BoardCutCalculator
import com.woodworking.calculatorpro.domain.LengthUnit
import com.woodworking.calculatorpro.ui.components.WCard
import com.woodworking.calculatorpro.ui.components.WField
import com.woodworking.calculatorpro.ui.components.WResultActions
import com.woodworking.calculatorpro.ui.components.WResultDivider
import com.woodworking.calculatorpro.ui.components.WResultRow
import com.woodworking.calculatorpro.ui.components.WScreenScaffold
import com.woodworking.calculatorpro.ui.components.WSegmented
import com.woodworking.calculatorpro.ui.screens.saveToHistory
import com.woodworking.calculatorpro.util.Clipboard
import com.woodworking.calculatorpro.util.Fmt
import com.woodworking.calculatorpro.util.parseDoubleOrNull
import com.woodworking.calculatorpro.util.parseIntOrNull
import kotlinx.coroutines.launch

private data class CutRow(val length: String, val qty: String)

@Composable
fun BoardCutScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var stockLen by remember { mutableStateOf("2440") }
    var kerf by remember { mutableStateOf("3") }
    var unit by remember { mutableStateOf(LengthUnit.MM) }
    val cuts: SnapshotStateList<CutRow> = remember {
        mutableStateListOf(
            CutRow("600", "4"),
            CutRow("450", "6"),
            CutRow("300", "8"),
        )
    }

    val stockMm = stockLen.parseDoubleOrNull()
    val kerfMm = kerf.parseDoubleOrNull()

    val parsedCuts = cuts.mapNotNull { row ->
        val l = row.length.parseDoubleOrNull()
        val q = row.qty.parseIntOrNull()
        if (l != null && q != null && l > 0 && q > 0) BoardCutCalculator.CutRequest(l, q) else null
    }

    val ready = stockMm != null && kerfMm != null && parsedCuts.isNotEmpty()
    val result = if (ready) {
        BoardCutCalculator.compute(
            BoardCutCalculator.Input(
                stockLength = stockMm!!,
                kerf = kerfMm!!,
                unit = unit,
                cuts = parsedCuts,
            )
        )
    } else null

    val title = stringResource(R.string.tool_boardcut)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = result?.let { r ->
        buildString {
            appendLine("Stocks needed: ${r.totalStocks}")
            appendLine("Cuts placed: ${r.totalCuts}")
            appendLine("Material efficiency: ${Fmt.percent(r.efficiency)}")
            appendLine("Total waste: ${Fmt.number(com.woodworking.calculatorpro.domain.convertLength(r.totalWasteMm, LengthUnit.MM, unit), 2)} ${unit.label}")
            r.stocks.forEach { s ->
                appendLine("Stock #${s.index}: ${s.cuts.joinToString(" + ") { Fmt.number(it, 2) }} (${unit.label}); leftover ${Fmt.number(s.remaining, 2)} ${unit.label}")
            }
            if (r.infeasibleCuts.isNotEmpty()) {
                appendLine("⚠ Cuts longer than stock: ${r.infeasibleCuts.joinToString { Fmt.number(it, 2) }} ${unit.label}")
            }
        }.trim()
    } ?: ""

    WScreenScaffold(title = title, onBack = onBack, snackbarHostState = snackbar) {
        WCard(title = stringResource(R.string.inputs)) {
            WSegmented(
                options = listOf(LengthUnit.MM, LengthUnit.CM, LengthUnit.M, LengthUnit.IN, LengthUnit.FT),
                selected = unit,
                onSelected = { unit = it },
                label = { it.label },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = stockLen,
                        onValueChange = { stockLen = it },
                        label = stringResource(R.string.board_stock_length),
                        trailingUnit = unit.label,
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = kerf,
                        onValueChange = { kerf = it },
                        label = stringResource(R.string.board_kerf),
                        trailingUnit = unit.label,
                    )
                }
            }
        }

        WCard(title = stringResource(R.string.tool_boardcut)) {
            cuts.forEachIndexed { idx, row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(2f)) {
                        WField(
                            value = row.length,
                            onValueChange = { cuts[idx] = row.copy(length = it) },
                            label = stringResource(R.string.board_cut_length),
                            trailingUnit = unit.label,
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        WField(
                            value = row.qty,
                            onValueChange = {
                                cuts[idx] = row.copy(qty = it.filter { c -> c.isDigit() })
                            },
                            label = stringResource(R.string.board_cut_qty),
                            keyboard = KeyboardType.Number,
                        )
                    }
                    IconButton(
                        onClick = { if (cuts.size > 1) cuts.removeAt(idx) },
                        enabled = cuts.size > 1,
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.action_delete))
                    }
                }
            }
            FilledTonalButton(
                onClick = { cuts.add(CutRow("", "1")) },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.board_add_cut))
            }
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null) {
                WResultRow(label = "—", value = "—")
            } else {
                WResultRow(
                    stringResource(R.string.board_total_stocks),
                    Fmt.integer(result.totalStocks),
                    accent = true,
                )
                WResultDivider()
                WResultRow(
                    stringResource(R.string.board_efficiency),
                    Fmt.percent(result.efficiency),
                    accent = true,
                )
                WResultDivider()
                WResultRow(
                    stringResource(R.string.board_total_waste),
                    "${Fmt.number(com.woodworking.calculatorpro.domain.convertLength(result.totalWasteMm, LengthUnit.MM, unit), 2)} ${unit.label}",
                )

                if (result.stocks.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.board_layout),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    result.stocks.forEach { stock -> StockRow(stock = stock, unit = unit, fullStock = stockMm ?: 0.0) }
                }

                if (result.infeasibleCuts.isNotEmpty()) {
                    Text(
                        text = "⚠ Cuts longer than stock are skipped",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "BoardCut", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "boardcut",
                            title = title,
                            summary = summary,
                            savedMessage = saved,
                        )
                    }
                },
                enabled = summary.isNotBlank(),
                copyLabel = stringResource(R.string.action_copy),
                saveLabel = stringResource(R.string.action_save),
            )
        }
    }
}

@Composable
private fun StockRow(
    stock: BoardCutCalculator.Stock,
    unit: LengthUnit,
    fullStock: Double,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Stock #${stock.index}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "leftover ${Fmt.number(stock.remaining, 2)} ${unit.label}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))

        // Render proportional bar so users see how the cuts fit. We treat
        // the stock as 100 % width and scale each cut to its share.
        val total = if (fullStock > 0.0) fullStock else (stock.cuts.sum() + stock.remaining).coerceAtLeast(1.0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            stock.cuts.forEach { cutLen ->
                val frac = (cutLen / total).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(frac.coerceAtLeast(0.001f))
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (frac > 0.07f) {
                        Text(
                            text = Fmt.number(cutLen, 1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
            val leftoverFrac = (stock.remaining / total).toFloat().coerceIn(0f, 1f)
            if (leftoverFrac > 0f) {
                Box(
                    modifier = Modifier
                        .weight(leftoverFrac)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
    }
}
