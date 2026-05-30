package com.woodworking.calculatorpro.ui.screens.miter

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
import com.woodworking.calculatorpro.domain.MiterCalculator
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

private enum class MiterMode { CORNER, POLYGON, COMPOUND }

/**
 * Miter angle calculator — three modes: simple corner, regular polygon and
 * compound (crown moulding). All computation happens inline as the user types.
 */
@Composable
fun MiterScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var mode by remember { mutableStateOf(MiterMode.CORNER) }
    var corner by remember { mutableStateOf("90") }
    var sides by remember { mutableStateOf("4") }
    var spring by remember { mutableStateOf("38") }
    var wall by remember { mutableStateOf("90") }

    // Live results computed from the current mode + inputs.
    val (resultLines, summary) = when (mode) {
        MiterMode.CORNER -> {
            val angle = corner.parseDoubleOrNull()
            if (angle == null || angle <= 0.0 || angle >= 180.0) {
                listOf<Triple<String, String, Boolean>>() to ""
            } else {
                val blade = MiterCalculator.cornerBladeAngle(angle)
                listOf(
                    Triple("Inside corner", "${Fmt.number(angle)}°", false),
                    Triple("Blade / miter cut", "${Fmt.number(blade)}°", true),
                    Triple("Per piece", "${Fmt.number(blade)}°", false),
                ) to "Inside corner: ${Fmt.number(angle)}°\nMiter cut: ${Fmt.number(blade)}°"
            }
        }
        MiterMode.POLYGON -> {
            val n = sides.parseIntOrNull()
            if (n == null || n < 3) {
                listOf<Triple<String, String, Boolean>>() to ""
            } else {
                val blade = MiterCalculator.polygonBladeAngle(n)
                val interior = MiterCalculator.polygonInteriorAngle(n)
                listOf(
                    Triple("Sides", n.toString(), false),
                    Triple("Interior angle", "${Fmt.number(interior)}°", false),
                    Triple("Blade / miter cut", "${Fmt.number(blade)}°", true),
                ) to "Polygon ($n sides)\nMiter cut: ${Fmt.number(blade)}°\nInterior: ${Fmt.number(interior)}°"
            }
        }
        MiterMode.COMPOUND -> {
            val s = spring.parseDoubleOrNull()
            val w = wall.parseDoubleOrNull()
            if (s == null || w == null || s !in 0.0..90.0 || w !in 0.0..180.0) {
                listOf<Triple<String, String, Boolean>>() to ""
            } else {
                val r = MiterCalculator.compound(s, w)
                listOf(
                    Triple("Spring", "${Fmt.number(s)}°", false),
                    Triple("Wall corner", "${Fmt.number(w)}°", false),
                    Triple(stringResource(R.string.miter_blade), "${Fmt.number(r.bladeAngleDeg)}°", true),
                    Triple(stringResource(R.string.miter_bevel), "${Fmt.number(r.bevelAngleDeg)}°", true),
                ) to ("Spring ${Fmt.number(s)}° · Wall ${Fmt.number(w)}°\n" +
                       "Blade ${Fmt.number(r.bladeAngleDeg)}° · Bevel ${Fmt.number(r.bevelAngleDeg)}°")
            }
        }
    }

    val title = stringResource(R.string.tool_miter)
    val copied = stringResource(R.string.copied)
    val saved = stringResource(R.string.saved)

    WScreenScaffold(
        title = title,
        onBack = onBack,
        snackbarHostState = snackbar,
    ) {
        WCard(title = stringResource(R.string.inputs)) {
            WSegmented(
                options = listOf(MiterMode.CORNER, MiterMode.POLYGON, MiterMode.COMPOUND),
                selected = mode,
                onSelected = { mode = it },
                label = { m ->
                    when (m) {
                        MiterMode.CORNER -> stringResource(R.string.miter_mode_corner)
                        MiterMode.POLYGON -> stringResource(R.string.miter_mode_polygon)
                        MiterMode.COMPOUND -> stringResource(R.string.miter_mode_compound)
                    }
                },
            )
            when (mode) {
                MiterMode.CORNER -> WField(
                    value = corner,
                    onValueChange = { corner = it },
                    label = stringResource(R.string.miter_corner_angle),
                    trailingUnit = "°",
                )
                MiterMode.POLYGON -> WField(
                    value = sides,
                    onValueChange = { sides = it.filter { c -> c.isDigit() } },
                    label = stringResource(R.string.miter_sides),
                    keyboard = androidx.compose.ui.text.input.KeyboardType.Number,
                )
                MiterMode.COMPOUND -> {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            WField(
                                value = spring,
                                onValueChange = { spring = it },
                                label = stringResource(R.string.miter_spring_angle),
                                trailingUnit = "°",
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            WField(
                                value = wall,
                                onValueChange = { wall = it },
                                label = stringResource(R.string.miter_wall_angle),
                                trailingUnit = "°",
                            )
                        }
                    }
                }
            }
        }

        WCard(title = stringResource(R.string.results)) {
            if (resultLines.isEmpty()) {
                WResultRow(label = "—", value = "—")
            } else {
                resultLines.forEachIndexed { index, (label, value, accent) ->
                    WResultRow(label = label, value = value, accent = accent)
                    if (index != resultLines.lastIndex) WResultDivider()
                }
            }

            WResultActions(
                onCopy = {
                    if (summary.isNotBlank()) {
                        Clipboard.copy(ctx, "Miter", summary)
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                },
                onSave = {
                    if (summary.isNotBlank()) {
                        saveToHistory(
                            scope = scope,
                            snackbar = snackbar,
                            toolKey = "miter",
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

