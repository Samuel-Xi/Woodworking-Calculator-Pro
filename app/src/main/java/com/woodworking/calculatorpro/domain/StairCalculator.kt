package com.woodworking.calculatorpro.domain

import kotlin.math.atan
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.PI

/**
 * Residential stair calculator. Uses the standard "preferred riser" approach:
 * pick the integer number of risers nearest to (totalRise / preferredRiser),
 * then derive everything else.
 *
 *  - number of risers      = round(totalRise / preferredRiser)
 *  - actual riser height   = totalRise / numRisers
 *  - number of treads      = numRisers - 1
 *  - tread depth           = totalRun / numTreads
 *  - stringer length       = √(rise² + run²)
 *  - pitch                 = atan(rise / run) in degrees
 *
 * The IRC suggests riser ≤ 7¾" (197 mm) and tread ≥ 10" (254 mm); we report
 * whether the calculated geometry is within that range.
 */
object StairCalculator {

    data class Input(
        val totalRise: Double,
        val totalRun: Double,
        val preferredRiser: Double,
        val unit: LengthUnit,
    )

    data class Result(
        val numRisers: Int,
        val numTreads: Int,
        val riserHeightMm: Double,
        val treadDepthMm: Double,
        val stringerLengthMm: Double,
        val pitchDeg: Double,
        val withinCode: Boolean,
    )

    /** IRC R311 typical residential limits in millimetres. */
    private const val MAX_RISER_MM = 196.85   // 7-3/4"
    private const val MIN_TREAD_MM = 254.0    // 10"

    fun compute(input: Input): Result {
        val riseMm = convertLength(input.totalRise, input.unit, LengthUnit.MM)
        val runMm  = convertLength(input.totalRun,  input.unit, LengthUnit.MM)
        val prefMm = convertLength(input.preferredRiser, input.unit, LengthUnit.MM)

        if (riseMm <= 0.0 || prefMm <= 0.0) {
            return Result(0, 0, 0.0, 0.0, 0.0, 0.0, false)
        }

        val numRisers = (riseMm / prefMm).roundToInt().coerceAtLeast(1)
        val numTreads = (numRisers - 1).coerceAtLeast(0)

        val riserH = riseMm / numRisers
        val treadD = if (numTreads > 0) runMm / numTreads else 0.0
        val stringer = hypot(riseMm, runMm)
        val pitch = if (runMm > 0.0) atan(riseMm / runMm) * 180.0 / PI else 90.0

        val codeOk = riserH <= MAX_RISER_MM && treadD >= MIN_TREAD_MM

        return Result(numRisers, numTreads, riserH, treadD, stringer, pitch, codeOk)
    }
}
