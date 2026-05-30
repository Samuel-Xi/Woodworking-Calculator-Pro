package com.woodworking.calculatorpro.ui.screens.convert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.woodworking.calculatorpro.domain.convertLength
import com.woodworking.calculatorpro.ui.components.WCard
import com.woodworking.calculatorpro.ui.components.WField
import com.woodworking.calculatorpro.ui.components.WResultDivider
import com.woodworking.calculatorpro.ui.components.WResultRow
import com.woodworking.calculatorpro.ui.components.WScreenScaffold
import com.woodworking.calculatorpro.ui.components.WSegmented
import com.woodworking.calculatorpro.util.Clipboard
import com.woodworking.calculatorpro.util.Fmt
import com.woodworking.calculatorpro.util.parseDoubleOrNull
import com.woodworking.calculatorpro.ui.components.WResultActions
import com.woodworking.calculatorpro.ui.screens.saveToHistory
import kotlinx.coroutines.launch

/**
 * Live unit converter. Whatever the user types in the source field is shown
 * in every other unit instantly, plus a "feet + inches" pretty form.
 */
@Composable
fun UnitConvertScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var amount by remember { mutableStateOf("100") }
    var from by remember { mutableStateOf(LengthUnit.MM) }

    val parsed = amount.parseDoubleOrNull()
    val mm = parsed?.let { from.toMm(it) }

    val title = stringResource(R.string.tool_convert)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = if (mm != null) {
        val rows = LengthUnit.values().joinToString("\n") { u ->
            "${Fmt.number(convertLength(parsed!!, from, u), 4)} ${u.label}"
        }
        "$amount ${from.label} =\n$rows\n${Fmt.feetInches(mm)}"
    } else ""

    WScreenScaffold(title = title, onBack = onBack, snackbarHostState = snackbar) {
        WCard(title = stringResource(R.string.inputs)) {
            WField(
                value = amount,
                onValueChange = { amount = it },
                label = stringResource(R.string.convert_amount),
                trailingUnit = from.label,
            )
            Text(
                text = stringResource(R.string.convert_from),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WSegmented(
                options = LengthUnit.values().toList(),
                selected = from,
                onSelected = { from = it },
                label = { it.label },
            )
        }

        WCard(title = stringResource(R.string.results)) {
            if (mm == null) {
                WResultRow(label = "—", value = "—")
            } else {
                LengthUnit.values().forEachIndexed { idx, u ->
                    val v = convertLength(parsed!!, from, u)
                    WResultRow(
                        label = u.label,
                        value = "${Fmt.number(v, 4)} ${u.label}",
                        accent = u == from,
                    )
                    if (idx != LengthUnit.values().lastIndex) WResultDivider()
                }
                WResultDivider()
                WResultRow(
                    label = "ft + in",
                    value = Fmt.feetInches(mm),
                )
            }
            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Convert", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "convert",
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
