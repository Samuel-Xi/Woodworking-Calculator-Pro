package com.woodworking.calculatorpro.ui.screens.spacing

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.domain.EqualSpacingCalculator
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

private enum class SpacingMode { COUNT, TARGET_GAP }

@Composable
fun SpacingScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var mode by remember { mutableStateOf(SpacingMode.COUNT) }
    var unit by remember { mutableStateOf(LengthUnit.IN) }
    var span by remember { mutableStateOf("96") }
    var itemWidth by remember { mutableStateOf("1.5") }
    var itemCount by remember { mutableStateOf("7") }
    var targetGap by remember { mutableStateOf("4") }

    val spanValue = span.parseDoubleOrNull()
    val itemWidthValue = itemWidth.parseDoubleOrNull() ?: 0.0
    val countValue = itemCount.parseIntOrNull()
    val targetGapValue = targetGap.parseDoubleOrNull()

    val result = if (spanValue != null && spanValue > 0 && itemWidthValue >= 0) {
        EqualSpacingCalculator.compute(
            EqualSpacingCalculator.Input(
                mode = if (mode == SpacingMode.COUNT) EqualSpacingCalculator.Mode.KNOWN_COUNT else EqualSpacingCalculator.Mode.TARGET_GAP,
                span = spanValue,
                itemWidth = itemWidthValue,
                itemCount = countValue,
                targetGap = targetGapValue,
            )
        )
    } else null

    val title = stringResource(R.string.tool_spacing)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)
    val marks = result?.positions.orEmpty().take(16).joinToString(", ") { Fmt.number(it, 2) }
    val marksSuffix = if ((result?.positions?.size ?: 0) > 16) " …" else ""

    val summary = result?.takeIf { it.valid }?.let { r ->
        buildString {
            appendLine("Items: ${r.itemCount}")
            appendLine("Equal gap: ${Fmt.number(r.gap, 3)} ${unit.label}")
            appendLine("Center-to-center: ${Fmt.number(r.centerToCenter, 3)} ${unit.label}")
            appendLine("First center: ${Fmt.number(r.firstCenter, 3)} ${unit.label}")
            appendLine("Marks from start: $marks$marksSuffix ${unit.label}")
        }.trim()
    } ?: ""

    WScreenScaffold(title = title, onBack = onBack, snackbarHostState = snackbar) {
        WCard(title = stringResource(R.string.inputs)) {
            WSegmented(
                options = listOf(SpacingMode.COUNT, SpacingMode.TARGET_GAP),
                selected = mode,
                onSelected = { mode = it },
                label = {
                    if (it == SpacingMode.COUNT) stringResource(R.string.spacing_mode_count)
                    else stringResource(R.string.spacing_mode_gap)
                },
            )
            WSegmented(
                options = listOf(LengthUnit.MM, LengthUnit.CM, LengthUnit.IN, LengthUnit.FT),
                selected = unit,
                onSelected = { unit = it },
                label = { it.label },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = span,
                        onValueChange = { span = it },
                        label = stringResource(R.string.spacing_span),
                        trailingUnit = unit.label,
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = itemWidth,
                        onValueChange = { itemWidth = it },
                        label = stringResource(R.string.spacing_item_width),
                        trailingUnit = unit.label,
                    )
                }
            }
            if (mode == SpacingMode.COUNT) {
                WField(
                    value = itemCount,
                    onValueChange = { itemCount = it.filter { c -> c.isDigit() } },
                    label = stringResource(R.string.spacing_item_count),
                    keyboard = KeyboardType.Number,
                )
            } else {
                WField(
                    value = targetGap,
                    onValueChange = { targetGap = it },
                    label = stringResource(R.string.spacing_target_gap),
                    trailingUnit = unit.label,
                )
            }
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null) {
                WResultRow(label = "—", value = "—")
            } else if (!result.valid) {
                WResultRow(
                    label = stringResource(R.string.spacing_warning),
                    value = stringResource(R.string.spacing_invalid),
                    accent = true,
                )
            } else {
                WResultRow(
                    label = stringResource(R.string.spacing_gap),
                    value = "${Fmt.number(result.gap, 3)} ${unit.label}",
                    accent = true,
                )
                WResultDivider()
                WResultRow(
                    label = stringResource(R.string.spacing_count),
                    value = Fmt.integer(result.itemCount),
                )
                WResultDivider()
                WResultRow(
                    label = stringResource(R.string.spacing_center),
                    value = "${Fmt.number(result.centerToCenter, 3)} ${unit.label}",
                )
                WResultDivider()
                WResultRow(
                    label = stringResource(R.string.spacing_first_center),
                    value = "${Fmt.number(result.firstCenter, 3)} ${unit.label}",
                )
                Text(
                    text = stringResource(R.string.spacing_marks),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "$marks$marksSuffix ${unit.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Equal Spacing", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "spacing",
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
