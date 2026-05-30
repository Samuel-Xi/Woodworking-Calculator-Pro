package com.woodworking.calculatorpro.ui.screens.paint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.domain.PaintCalculator
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

@Composable
fun PaintScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var system by remember { mutableStateOf(PaintCalculator.System.METRIC) }
    var wallLen by remember { mutableStateOf("12") }
    var wallH by remember { mutableStateOf("2.7") }
    var openings by remember { mutableStateOf("4") }
    var coverage by remember { mutableStateOf("10") }      // m²/L typical: 8–12
    var coverageImp by remember { mutableStateOf("350") }  // ft²/gal typical
    var coats by remember { mutableStateOf("2") }

    val L = wallLen.parseDoubleOrNull()
    val H = wallH.parseDoubleOrNull()
    val O = openings.parseDoubleOrNull() ?: 0.0
    val cv = if (system == PaintCalculator.System.METRIC) coverage.parseDoubleOrNull()
             else coverageImp.parseDoubleOrNull()
    val n = coats.parseIntOrNull()

    val ready = L != null && H != null && cv != null && n != null && L > 0 && H > 0 && cv > 0 && n > 0

    val result = if (ready) {
        PaintCalculator.compute(
            PaintCalculator.Input(
                system = system,
                wallLength = L!!,
                wallHeight = H!!,
                openings = O,
                coverage = cv!!,
                coats = n!!,
            )
        )
    } else null

    val title = stringResource(R.string.tool_paint)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = result?.let { r ->
        buildString {
            appendLine("Net surface: ${Fmt.number(r.netArea, 2)} ${r.areaUnitLabel}")
            appendLine("Coats: $n")
            appendLine("Total: ${Fmt.number(r.totalVolume, 2)} ${r.volumeUnitLabel}")
            appendLine("Buy at least: ${PaintCalculator.roundUpToWholeUnit(r.totalVolume)} ${r.volumeUnitLabel}")
        }.trim()
    } ?: ""

    val lengthUnit = if (system == PaintCalculator.System.METRIC) "m" else "ft"
    val areaUnit = if (system == PaintCalculator.System.METRIC) "m²" else "ft²"
    val coverageUnit = if (system == PaintCalculator.System.METRIC) "m²/L" else "ft²/gal"

    WScreenScaffold(title = title, onBack = onBack, snackbarHostState = snackbar) {
        WCard(title = stringResource(R.string.inputs)) {
            WSegmented(
                options = listOf(PaintCalculator.System.METRIC, PaintCalculator.System.IMPERIAL),
                selected = system,
                onSelected = { system = it },
                label = {
                    if (it == PaintCalculator.System.METRIC) stringResource(R.string.paint_unit_metric)
                    else stringResource(R.string.paint_unit_imperial)
                },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = wallLen,
                        onValueChange = { wallLen = it },
                        label = stringResource(R.string.paint_wall_length),
                        trailingUnit = lengthUnit,
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = wallH,
                        onValueChange = { wallH = it },
                        label = stringResource(R.string.paint_wall_height),
                        trailingUnit = lengthUnit,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = openings,
                        onValueChange = { openings = it },
                        label = stringResource(R.string.paint_openings),
                        trailingUnit = areaUnit,
                    )
                }
                Column(Modifier.weight(1f)) {
                    if (system == PaintCalculator.System.METRIC) {
                        WField(
                            value = coverage,
                            onValueChange = { coverage = it },
                            label = stringResource(R.string.paint_coverage),
                            trailingUnit = coverageUnit,
                        )
                    } else {
                        WField(
                            value = coverageImp,
                            onValueChange = { coverageImp = it },
                            label = stringResource(R.string.paint_coverage),
                            trailingUnit = coverageUnit,
                        )
                    }
                }
            }
            WField(
                value = coats,
                onValueChange = { coats = it.filter { c -> c.isDigit() } },
                label = stringResource(R.string.paint_coats),
                keyboard = KeyboardType.Number,
            )
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null) {
                WResultRow(label = "—", value = "—")
            } else {
                WResultRow(
                    stringResource(R.string.paint_net_area),
                    "${Fmt.number(result.netArea, 2)} ${result.areaUnitLabel}",
                )
                WResultDivider()
                WResultRow(
                    stringResource(R.string.paint_total),
                    "${Fmt.number(result.totalVolume, 2)} ${result.volumeUnitLabel}",
                    accent = true,
                )
                WResultDivider()
                WResultRow(
                    "Round up to whole ${result.volumeUnitLabel}",
                    "${PaintCalculator.roundUpToWholeUnit(result.totalVolume)} ${result.volumeUnitLabel}",
                )
            }
            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Paint", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "paint",
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
