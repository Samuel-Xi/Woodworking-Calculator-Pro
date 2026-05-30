package com.woodworking.calculatorpro.domain

/**
 * Length units used across the app. Everything converts to and from millimetres
 * internally; keeping a single canonical unit keeps the calculators simple.
 */
enum class LengthUnit(val label: String, val mmPerUnit: Double) {
    MM("mm", 1.0),
    CM("cm", 10.0),
    M("m",  1000.0),
    IN("in", 25.4),
    FT("ft", 304.8);

    fun toMm(value: Double): Double = value * mmPerUnit
    fun fromMm(mm: Double): Double = mm / mmPerUnit
}

/** Convert [value] expressed in [from] units to [to] units. */
fun convertLength(value: Double, from: LengthUnit, to: LengthUnit): Double =
    to.fromMm(from.toMm(value))

/** Area units derived from length units (so we can keep a single source of truth). */
enum class AreaUnit(val label: String, val m2PerUnit: Double) {
    M2("m²",   1.0),
    FT2("ft²", 0.09290304),
    IN2("in²", 0.00064516),
    CM2("cm²", 0.0001);

    fun toM2(value: Double): Double = value * m2PerUnit
    fun fromM2(m2: Double): Double = m2 / m2PerUnit
}

fun convertArea(value: Double, from: AreaUnit, to: AreaUnit): Double =
    to.fromM2(from.toM2(value))

/** Volume conversion helper — only used by the paint calculator for gal ⇄ L. */
object VolumeConverter {
    const val LITERS_PER_GALLON_US = 3.785411784
    fun galToLiters(gal: Double) = gal * LITERS_PER_GALLON_US
    fun litersToGal(l: Double) = l / LITERS_PER_GALLON_US
}
