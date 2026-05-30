package com.woodworking.calculatorpro.ui.screens.flooring

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
import com.woodworking.calculatorpro.domain.FlooringCalculator
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

@Composable
fun FlooringScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var roomLen by remember { mutableStateOf("4") }
    var roomWid by remember { mutableStateOf("3") }
    var roomUnit by remember { mutableStateOf(LengthUnit.M) }

    var plankLen by remember { mutableStateOf("1200") }
    var plankWid by remember { mutableStateOf("190") }
    var plankUnit by remember { mutableStateOf(LengthUnit.MM) }

    var waste by remember { mutableStateOf("10") }
    var perBox by remember { mutableStateOf("8") }

    val rL = roomLen.parseDoubleOrNull()
    val rW = roomWid.parseDoubleOrNull()
    val pL = plankLen.parseDoubleOrNull()
    val pW = plankWid.parseDoubleOrNull()
    val w = waste.parseDoubleOrNull()?.div(100.0)?.coerceIn(0.0, 1.0)
    val box = perBox.parseIntOrNull()

    val ready = rL != null && rW != null && pL != null && pW != null && w != null

    val result = if (ready) {
        FlooringCalculator.compute(
            FlooringCalculator.Input(
                roomLength = rL!!,
                roomWidth = rW!!,
                roomUnit = roomUnit,
                plankLength = pL!!,
                plankWidth = pW!!,
                plankUnit = plankUnit,
                wastePercent = w!!,
                piecesPerBox = box,
            )
        )
    } else null

    val title = stringResource(R.string.tool_flooring)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = result?.let { r ->
        val wastePct = Fmt.number((w ?: 0.0) * 100)
        buildString {
            appendLine("Room: ${Fmt.number(r.roomAreaM2, 2)} m²")
            appendLine("Piece: ${Fmt.number(r.pieceAreaM2, 3)} m²")
            appendLine("Pieces (no waste): ${r.piecesBase}")
            appendLine("Pieces (with $wastePct% waste): ${r.piecesWithWaste}")
            r.boxesNeeded?.let { appendLine("Boxes: $it") }
        }.trim()
    } ?: ""

    WScreenScaffold(title = title, onBack = onBack, snackbarHostState = snackbar) {
        WCard(title = stringResource(R.string.inputs)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = roomLen,
                        onValueChange = { roomLen = it },
                        label = stringResource(R.string.floor_room_length),
                        trailingUnit = roomUnit.label,
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = roomWid,
                        onValueChange = { roomWid = it },
                        label = stringResource(R.string.floor_room_width),
                        trailingUnit = roomUnit.label,
                    )
                }
            }
            UnitSelector(value = roomUnit, onSelect = { roomUnit = it })

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = plankLen,
                        onValueChange = { plankLen = it },
                        label = stringResource(R.string.floor_plank_length),
                        trailingUnit = plankUnit.label,
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = plankWid,
                        onValueChange = { plankWid = it },
                        label = stringResource(R.string.floor_plank_width),
                        trailingUnit = plankUnit.label,
                    )
                }
            }
            UnitSelector(value = plankUnit, onSelect = { plankUnit = it })

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    WField(
                        value = waste,
                        onValueChange = { waste = it },
                        label = stringResource(R.string.floor_waste),
                        trailingUnit = "%",
                    )
                }
                Column(Modifier.weight(1f)) {
                    WField(
                        value = perBox,
                        onValueChange = { perBox = it.filter { c -> c.isDigit() } },
                        label = stringResource(R.string.floor_box_count),
                        keyboard = KeyboardType.Number,
                    )
                }
            }
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null) {
                WResultRow(label = "—", value = "—")
            } else {
                WResultRow(stringResource(R.string.floor_room_area), "${Fmt.number(result.roomAreaM2, 2)} m²")
                WResultDivider()
                WResultRow(stringResource(R.string.floor_piece_area), "${Fmt.number(result.pieceAreaM2, 3)} m²")
                WResultDivider()
                WResultRow(stringResource(R.string.floor_pieces_base), Fmt.integer(result.piecesBase))
                WResultDivider()
                WResultRow(
                    stringResource(R.string.floor_pieces_total),
                    Fmt.integer(result.piecesWithWaste),
                    accent = true,
                )
                if (result.boxesNeeded != null) {
                    WResultDivider()
                    WResultRow(
                        stringResource(R.string.floor_boxes),
                        Fmt.integer(result.boxesNeeded),
                        accent = true,
                    )
                }
            }
            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Flooring", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "flooring",
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
private fun UnitSelector(value: LengthUnit, onSelect: (LengthUnit) -> Unit) {
    WSegmented(
        options = listOf(LengthUnit.MM, LengthUnit.CM, LengthUnit.M, LengthUnit.IN, LengthUnit.FT),
        selected = value,
        onSelected = onSelect,
        label = { it.label },
    )
}
