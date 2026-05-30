package com.woodworking.calculatorpro.ui.screens.stair

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.domain.LengthUnit
import com.woodworking.calculatorpro.domain.StairCalculator
import com.woodworking.calculatorpro.domain.convertLength
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
import kotlinx.coroutines.launch

@Composable
fun StairScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var unit by remember { mutableStateOf(LengthUnit.MM) }
    // Sensible defaults for a typical residential staircase (≈ 2.7 m floor-to-floor).
    var rise by remember { mutableStateOf("2700") }
    var run by remember { mutableStateOf("3500") }
    var pref by remember { mutableStateOf("180") }

    val rD = rise.parseDoubleOrNull()
    val nD = run.parseDoubleOrNull()
    val pD = pref.parseDoubleOrNull()

    val ready = rD != null && nD != null && pD != null && rD > 0 && pD > 0
    val result = if (ready) {
        StairCalculator.compute(
            StairCalculator.Input(
                totalRise = rD!!,
                totalRun = nD!!,
                preferredRiser = pD!!,
                unit = unit,
            )
        )
    } else null

    fun mmAsUnit(mm: Double): String =
        "${Fmt.number(convertLength(mm, LengthUnit.MM, unit), 2)} ${unit.label}"

    val title = stringResource(R.string.tool_stair)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = result?.takeIf { it.numRisers > 0 }?.let { r ->
        buildString {
            appendLine("Risers: ${r.numRisers} · Treads: ${r.numTreads}")
            appendLine("Riser height: ${mmAsUnit(r.riserHeightMm)}")
            appendLine("Tread depth: ${mmAsUnit(r.treadDepthMm)}")
            appendLine("Stringer: ${mmAsUnit(r.stringerLengthMm)}")
            appendLine("Pitch: ${Fmt.number(r.pitchDeg, 2)}°")
            appendLine(if (r.withinCode) "Within IRC residential limits" else "Outside common residential code")
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
                        value = rise,
                        onValueChange = { rise = it },
                        label = stringResource(R.string.stair_total_rise),
                        trailingUnit = unit.label,
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = run,
                        onValueChange = { run = it },
                        label = stringResource(R.string.stair_total_run),
                        trailingUnit = unit.label,
                    )
                }
            }
            WField(
                value = pref,
                onValueChange = { pref = it },
                label = stringResource(R.string.stair_pref_riser),
                trailingUnit = unit.label,
                supporting = "Typical: 175–185 mm (7–7¼ in)",
            )
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null || result.numRisers == 0) {
                WResultRow(label = "—", value = "—")
            } else {
                WResultRow(stringResource(R.string.stair_num_risers), Fmt.integer(result.numRisers), accent = true)
                WResultDivider()
                WResultRow(stringResource(R.string.stair_num_treads), Fmt.integer(result.numTreads))
                WResultDivider()
                WResultRow(stringResource(R.string.stair_riser_height), mmAsUnit(result.riserHeightMm), accent = true)
                WResultDivider()
                WResultRow(stringResource(R.string.stair_tread_depth), mmAsUnit(result.treadDepthMm), accent = true)
                WResultDivider()
                WResultRow(stringResource(R.string.stair_stringer), mmAsUnit(result.stringerLengthMm))
                WResultDivider()
                WResultRow(stringResource(R.string.stair_pitch), "${Fmt.number(result.pitchDeg, 2)}°")

                Text(
                    text = if (result.withinCode)
                        stringResource(R.string.stair_code_ok)
                    else stringResource(R.string.stair_code_warn),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (result.withinCode) MaterialTheme.colorScheme.tertiary
                             else MaterialTheme.colorScheme.error,
                )
            }

            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Stair", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "stair",
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
