package com.woodworking.calculatorpro.domain

import kotlin.math.ceil

/**
 * Ceiling joist / runner spacing calculator.
 *
 *   joists  = floor(span / spacing) + 1   (one at each end)
 *   total   = joists × roomLength
 *
 * The "room width" is measured perpendicular to the joists, so spacing applies
 * across that dimension. Cross runners follow the same formula in the other
 * direction when [crossRunnerSpacing] > 0.
 */
object CeilingCalculator {

    data class Input(
        val roomLength: Double,
        val roomWidth: Double,
        val spacing: Double,
        val unit: LengthUnit,
        val crossRunnerSpacing: Double = 0.0,
    )

    data class Result(
        val joistCount: Int,
        val joistLinearMm: Double,
        val crossCount: Int,
        val crossLinearMm: Double,
        val unit: LengthUnit,
    )

    fun compute(input: Input): Result {
        val lenMm  = convertLength(input.roomLength, input.unit, LengthUnit.MM)
        val widMm  = convertLength(input.roomWidth,  input.unit, LengthUnit.MM)
        val spMm   = convertLength(input.spacing,    input.unit, LengthUnit.MM)
        val crossSpMm = convertLength(input.crossRunnerSpacing, input.unit, LengthUnit.MM)

        if (lenMm <= 0 || widMm <= 0 || spMm <= 0) {
            return Result(0, 0.0, 0, 0.0, input.unit)
        }

        // Joists are placed every `spMm` across the width, plus one for the wall.
        val joists = (kotlin.math.floor(widMm / spMm).toInt() + 1).coerceAtLeast(2)
        val joistLinear = joists * lenMm

        val cross = if (crossSpMm > 0) {
            (kotlin.math.floor(lenMm / crossSpMm).toInt() + 1).coerceAtLeast(2)
        } else 0
        val crossLinear = cross * widMm

        return Result(joists, joistLinear, cross, crossLinear, input.unit)
    }
}
