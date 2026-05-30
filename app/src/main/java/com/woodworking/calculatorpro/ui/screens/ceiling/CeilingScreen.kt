package com.woodworking.calculatorpro.ui.screens.ceiling

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
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.domain.CeilingCalculator
import com.woodworking.calculatorpro.domain.LengthUnit
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
fun CeilingScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var unit by remember { mutableStateOf(LengthUnit.MM) }
    var len by remember { mutableStateOf("5000") }
    var wid by remember { mutableStateOf("4000") }
    var spacing by remember { mutableStateOf("400") }
    var crossSpacing by remember { mutableStateOf("0") }

    val L = len.parseDoubleOrNull()
    val W = wid.parseDoubleOrNull()
    val S = spacing.parseDoubleOrNull()
    val C = crossSpacing.parseDoubleOrNull() ?: 0.0

    val ready = L != null && W != null && S != null && L > 0 && W > 0 && S > 0

    val result = if (ready) {
        CeilingCalculator.compute(
            CeilingCalculator.Input(
                roomLength = L!!,
                roomWidth = W!!,
                spacing = S!!,
                unit = unit,
                crossRunnerSpacing = C,
            )
        )
    } else null

    fun mmAsUnit(mm: Double): String =
        "${Fmt.number(convertLength(mm, LengthUnit.MM, unit), 2)} ${unit.label}"

    val title = stringResource(R.string.tool_ceiling)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = result?.takeIf { it.joistCount > 0 }?.let { r ->
        buildString {
            appendLine("Joists: ${r.joistCount}")
            appendLine("Total joist length: ${mmAsUnit(r.joistLinearMm)}")
            if (r.crossCount > 0) {
                appendLine("Cross runners: ${r.crossCount}")
                appendLine("Total cross length: ${mmAsUnit(r.crossLinearMm)}")
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
                        value = len,
                        onValueChange = { len = it },
                        label = stringResource(R.string.ceiling_room_length),
                        trailingUnit = unit.label,
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = wid,
                        onValueChange = { wid = it },
                        label = stringResource(R.string.ceiling_room_width),
                        trailingUnit = unit.label,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = spacing,
                        onValueChange = { spacing = it },
                        label = stringResource(R.string.ceiling_spacing),
                        trailingUnit = unit.label,
                        supporting = "Common: 400 mm / 16 in OC",
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = crossSpacing,
                        onValueChange = { crossSpacing = it },
                        label = stringResource(R.string.ceiling_runners),
                        trailingUnit = unit.label,
                        supporting = "0 = none",
                    )
                }
            }
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null || result.joistCount == 0) {
                WResultRow(label = "—", value = "—")
            } else {
                WResultRow(stringResource(R.string.ceiling_count), Fmt.integer(result.joistCount), accent = true)
                WResultDivider()
                WResultRow(stringResource(R.string.ceiling_linear), mmAsUnit(result.joistLinearMm), accent = true)
                if (result.crossCount > 0) {
                    WResultDivider()
                    WResultRow(stringResource(R.string.ceiling_runners), Fmt.integer(result.crossCount))
                    WResultDivider()
                    WResultRow(
                        stringResource(R.string.ceiling_linear) + " (cross)",
                        mmAsUnit(result.crossLinearMm),
                    )
                }
            }
            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Ceiling", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "ceiling",
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
