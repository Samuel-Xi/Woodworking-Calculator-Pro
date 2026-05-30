package com.woodworking.calculatorpro.ui.screens.boardfeet

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
import com.woodworking.calculatorpro.domain.BoardFootCalculator
import com.woodworking.calculatorpro.ui.components.WCard
import com.woodworking.calculatorpro.ui.components.WField
import com.woodworking.calculatorpro.ui.components.WResultActions
import com.woodworking.calculatorpro.ui.components.WResultDivider
import com.woodworking.calculatorpro.ui.components.WResultRow
import com.woodworking.calculatorpro.ui.components.WScreenScaffold
import com.woodworking.calculatorpro.ui.screens.saveToHistory
import com.woodworking.calculatorpro.util.Clipboard
import com.woodworking.calculatorpro.util.Fmt
import com.woodworking.calculatorpro.util.parseDoubleOrNull
import com.woodworking.calculatorpro.util.parseIntOrNull
import kotlinx.coroutines.launch

@Composable
fun BoardFeetScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var thickness by remember { mutableStateOf("1.5") }
    var width by remember { mutableStateOf("5.5") }
    var length by remember { mutableStateOf("8") }
    var quantity by remember { mutableStateOf("6") }
    var waste by remember { mutableStateOf("10") }
    var price by remember { mutableStateOf("4.25") }

    val t = thickness.parseDoubleOrNull()
    val w = width.parseDoubleOrNull()
    val l = length.parseDoubleOrNull()
    val q = quantity.parseIntOrNull()
    val wastePct = waste.parseDoubleOrNull() ?: 0.0
    val priceValue = price.parseDoubleOrNull()

    val ready = t != null && w != null && l != null && q != null && t > 0 && w > 0 && l > 0 && q > 0
    val result = if (ready) {
        BoardFootCalculator.compute(
            BoardFootCalculator.Input(
                thicknessIn = t!!,
                widthIn = w!!,
                lengthFt = l!!,
                quantity = q!!,
                wastePercent = wastePct,
                pricePerBoardFoot = priceValue,
            )
        )
    } else null

    val title = stringResource(R.string.tool_boardfeet)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = result?.let { r ->
        buildString {
            appendLine("Board feet per piece: ${Fmt.number(r.perPieceBoardFeet, 3)} bd ft")
            appendLine("Base board feet: ${Fmt.number(r.baseBoardFeet, 3)} bd ft")
            appendLine("Waste: ${Fmt.number(r.wasteBoardFeet, 3)} bd ft")
            appendLine("Total board feet: ${Fmt.number(r.totalBoardFeet, 3)} bd ft")
            r.estimatedCost?.let { appendLine("Estimated cost: ${Fmt.money(it)}") }
        }.trim()
    } ?: ""

    WScreenScaffold(title = title, onBack = onBack, snackbarHostState = snackbar) {
        WCard(title = stringResource(R.string.inputs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = thickness,
                        onValueChange = { thickness = it },
                        label = stringResource(R.string.boardfeet_thickness),
                        trailingUnit = "in",
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = width,
                        onValueChange = { width = it },
                        label = stringResource(R.string.boardfeet_width),
                        trailingUnit = "in",
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = length,
                        onValueChange = { length = it },
                        label = stringResource(R.string.boardfeet_length),
                        trailingUnit = "ft",
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                        label = stringResource(R.string.boardfeet_quantity),
                        keyboard = KeyboardType.Number,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = waste,
                        onValueChange = { waste = it },
                        label = stringResource(R.string.boardfeet_waste),
                        trailingUnit = "%",
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = price,
                        onValueChange = { price = it },
                        label = stringResource(R.string.boardfeet_price),
                        trailingUnit = "$/bd ft",
                    )
                }
            }
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null) {
                WResultRow(label = "—", value = "—")
            } else {
                WResultRow(
                    label = stringResource(R.string.boardfeet_total),
                    value = "${Fmt.number(result.totalBoardFeet, 3)} bd ft",
                    accent = true,
                )
                WResultDivider()
                WResultRow(
                    label = stringResource(R.string.boardfeet_each),
                    value = "${Fmt.number(result.perPieceBoardFeet, 3)} bd ft",
                )
                WResultDivider()
                WResultRow(
                    label = stringResource(R.string.boardfeet_base),
                    value = "${Fmt.number(result.baseBoardFeet, 3)} bd ft",
                )
                WResultDivider()
                WResultRow(
                    label = stringResource(R.string.boardfeet_waste_amount),
                    value = "${Fmt.number(result.wasteBoardFeet, 3)} bd ft",
                )
                result.estimatedCost?.let {
                    WResultDivider()
                    WResultRow(
                        label = stringResource(R.string.boardfeet_cost),
                        value = Fmt.money(it),
                        accent = true,
                    )
                }
            }
            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Board Feet", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "boardfeet",
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
