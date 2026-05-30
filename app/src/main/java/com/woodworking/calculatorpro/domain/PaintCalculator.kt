package com.woodworking.calculatorpro.domain

import kotlin.math.ceil

/**
 * Paint estimator.
 *
 * Net surface area = wallLength × wallHeight − openings
 * Volume needed    = netArea × coats / coverageRate
 *
 * Two unit systems are supported:
 *  - Metric:    length in metres → m² and litres
 *  - Imperial:  length in feet   → ft² and US gallons
 */
object PaintCalculator {

    enum class System { METRIC, IMPERIAL }

    data class Input(
        val system: System,
        /** wall length in m (metric) or ft (imperial). */
        val wallLength: Double,
        /** wall height in m or ft. */
        val wallHeight: Double,
        /** opening area to subtract (m² or ft²). */
        val openings: Double,
        /** coverage rate: m²/L for metric, ft²/gal for imperial. */
        val coverage: Double,
        val coats: Int,
    )

    data class Result(
        val netArea: Double,
        val totalVolume: Double, // L for metric, gal for imperial
        val areaUnitLabel: String,
        val volumeUnitLabel: String,
    )

    fun compute(input: Input): Result {
        val gross = (input.wallLength * input.wallHeight).coerceAtLeast(0.0)
        val net = (gross - input.openings.coerceAtLeast(0.0)).coerceAtLeast(0.0)
        val coats = input.coats.coerceAtLeast(1)

        val total = if (input.coverage > 0.0)
            (net * coats) / input.coverage else 0.0

        val areaLabel = if (input.system == System.METRIC) "m²" else "ft²"
        val volumeLabel = if (input.system == System.METRIC) "L" else "gal"

        return Result(
            netArea = net,
            totalVolume = total,
            areaUnitLabel = areaLabel,
            volumeUnitLabel = volumeLabel,
        )
    }

    /** Convenience to round volume up to the nearest can / quart for procurement. */
    fun roundUpToWholeUnit(volume: Double): Int = ceil(volume).toInt()
}
