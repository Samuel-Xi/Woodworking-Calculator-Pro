package com.woodworking.calculatorpro.ui.screens.sheetcut

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.domain.LengthUnit
import com.woodworking.calculatorpro.domain.SheetCutCalculator
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

/**
 * 2-D sheet-goods cutting screen. Users enter the master sheet size, kerf,
 * and a list of parts (length × width × qty). The result includes the
 * per-sheet layout plus a thumbnail diagram for each open sheet so the
 * carpenter can mark up a real board the same way.
 */
private data class PartRow(val length: String, val width: String, val qty: String, val label: String)

@Composable
fun SheetCutScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var sheetLength by remember { mutableStateOf("2440") }
    var sheetWidth  by remember { mutableStateOf("1220") }
    var kerf        by remember { mutableStateOf("3") }
    var unit by remember { mutableStateOf(LengthUnit.MM) }

    // Sensible starter cabinet job: 4 side panels, 2 tops, 4 shelves.
    val parts: SnapshotStateList<PartRow> = remember {
        mutableStateListOf(
            PartRow("760", "560", "4", "Side"),
            PartRow("900", "560", "2", "Top"),
            PartRow("864", "540", "4", "Shelf"),
        )
    }

    val sheetLen = sheetLength.parseDoubleOrNull()
    val sheetWid = sheetWidth.parseDoubleOrNull()
    val kerfVal  = kerf.parseDoubleOrNull()

    val parsedParts = parts.mapNotNull { row ->
        val l = row.length.parseDoubleOrNull()
        val w = row.width.parseDoubleOrNull()
        val q = row.qty.parseIntOrNull()
        if (l != null && w != null && q != null && l > 0 && w > 0 && q > 0) {
            SheetCutCalculator.Part(
                length = l,
                width = w,
                quantity = q,
                label = row.label.trim().ifEmpty { null },
            )
        } else null
    }

    val ready = sheetLen != null && sheetWid != null && kerfVal != null && parsedParts.isNotEmpty()
    val result = if (ready) {
        SheetCutCalculator.compute(
            SheetCutCalculator.Input(
                sheetLength = sheetLen!!,
                sheetWidth = sheetWid!!,
                kerf = kerfVal!!,
                unit = unit,
                parts = parsedParts,
            )
        )
    } else null

    val title = stringResource(R.string.tool_sheetcut)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    val summary = result?.let { r ->
        buildString {
            appendLine("Sheets needed: ${r.totalSheets}")
            appendLine("Parts placed: ${r.placedParts} / ${r.totalParts}")
            appendLine("Material efficiency: ${Fmt.percent(r.efficiency)}")
            r.sheets.forEach { sheet ->
                appendLine("Sheet #${sheet.index}:")
                sheet.placements.forEach { p ->
                    appendLine(
                        "  ${p.label ?: "Part"} ${Fmt.number(p.length, 1)} × ${Fmt.number(p.width, 1)} " +
                            "${unit.label} at (${Fmt.number(p.x, 1)}, ${Fmt.number(p.y, 1)})" +
                            if (p.rotated) " [rotated]" else ""
                    )
                }
            }
            if (r.skippedParts.isNotEmpty()) {
                appendLine("⚠ Skipped (too large): ${r.skippedParts.joinToString { "${it.length}×${it.width}" }}")
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
                Box(Modifier.weight(1f)) {
                    WField(
                        value = sheetLength,
                        onValueChange = { sheetLength = it },
                        label = stringResource(R.string.sheet_sheet_length),
                        trailingUnit = unit.label,
                    )
                }
                Box(Modifier.weight(1f)) {
                    WField(
                        value = sheetWidth,
                        onValueChange = { sheetWidth = it },
                        label = stringResource(R.string.sheet_sheet_width),
                        trailingUnit = unit.label,
                    )
                }
            }
            WField(
                value = kerf,
                onValueChange = { kerf = it },
                label = stringResource(R.string.sheet_kerf),
                trailingUnit = unit.label,
            )
        }

        WCard(title = stringResource(R.string.tool_sheetcut)) {
            parts.forEachIndexed { idx, row ->
                PartRowFields(
                    row = row,
                    unit = unit,
                    onChange = { parts[idx] = it },
                    onRemove = { if (parts.size > 1) parts.removeAt(idx) },
                    removable = parts.size > 1,
                )
            }
            FilledTonalButton(
                onClick = { parts.add(PartRow("", "", "1", "")) },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.sheet_add_part))
            }
        }

        WCard(title = stringResource(R.string.results)) {
            if (result == null) {
                WResultRow(label = "—", value = "—")
            } else {
                WResultRow(
                    stringResource(R.string.sheet_sheets_needed),
                    Fmt.integer(result.totalSheets),
                    accent = true,
                )
                WResultDivider()
                WResultRow(
                    stringResource(R.string.sheet_efficiency),
                    Fmt.percent(result.efficiency),
                    accent = true,
                )
                WResultDivider()
                WResultRow(
                    stringResource(R.string.sheet_parts_placed),
                    "${result.placedParts} / ${result.totalParts}",
                )
                if (result.skippedParts.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.sheet_parts_skipped) +
                            ": " + result.skippedParts.joinToString {
                                "${Fmt.number(it.length, 1)}×${Fmt.number(it.width, 1)} ${unit.label}"
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (result.sheets.isNotEmpty() && sheetLen != null && sheetWid != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.sheet_layout),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    result.sheets.forEach { sheet ->
                        SheetDiagram(
                            sheet = sheet,
                            sheetLength = sheetLen,
                            sheetWidth = sheetWid,
                        )
                    }
                }
            }

            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "SheetCut", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "sheetcut",
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
private fun PartRowFields(
    row: PartRow,
    unit: LengthUnit,
    onChange: (PartRow) -> Unit,
    onRemove: () -> Unit,
    removable: Boolean,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(2f)) {
            WField(
                value = row.length,
                onValueChange = { onChange(row.copy(length = it)) },
                label = stringResource(R.string.sheet_part_length),
                trailingUnit = unit.label,
            )
        }
        Box(Modifier.weight(2f)) {
            WField(
                value = row.width,
                onValueChange = { onChange(row.copy(width = it)) },
                label = stringResource(R.string.sheet_part_width),
                trailingUnit = unit.label,
            )
        }
        Box(Modifier.weight(1f)) {
            WField(
                value = row.qty,
                onValueChange = { onChange(row.copy(qty = it.filter { c -> c.isDigit() })) },
                label = stringResource(R.string.sheet_part_qty),
                keyboard = KeyboardType.Number,
            )
        }
        IconButton(
            onClick = onRemove,
            enabled = removable,
        ) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.action_delete))
        }
    }
}

/**
 * Top-down diagram of a single sheet. Aspect ratio is locked to the real
 * sheet dimensions so the layout reads honestly. Each placement is rendered
 * with the brand primary tint and labelled when there's room.
 */
@Composable
private fun SheetDiagram(
    sheet: SheetCutCalculator.Sheet,
    sheetLength: Double,
    sheetWidth: Double,
) {
    val ratio = (sheetLength / sheetWidth).toFloat().coerceIn(0.3f, 5f)
    val board = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outlineVariant
    val fill = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
    val strokeColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)

    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Text(
            text = "Sheet #${sheet.index}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio)
                .clip(RoundedCornerShape(10.dp))
                .background(board),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val sx = size.width / sheetLength.toFloat()
                val sy = size.height / sheetWidth.toFloat()

                // Sheet outline.
                drawRect(
                    color = outline,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height),
                    style = Stroke(width = 1.5f),
                )
                // Each part placement.
                sheet.placements.forEach { p ->
                    val left = (p.x * sx).toFloat()
                    val top = (p.y * sy).toFloat()
                    val w = (p.length * sx).toFloat()
                    val h = (p.width * sy).toFloat()
                    drawRect(
                        color = fill,
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                    )
                    drawRect(
                        color = strokeColor,
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        style = Stroke(width = 1f),
                    )
                }
            }
        }
    }
}
