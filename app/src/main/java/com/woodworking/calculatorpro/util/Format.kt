package com.woodworking.calculatorpro.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * Centralised number formatting. We always use a US-style decimal separator
 * because the calculators are unit-aware and engineering-oriented; mixing
 * locales here would introduce ambiguity for shared screenshots.
 */
object Fmt {

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val twoDp = DecimalFormat("#,##0.##", symbols)
    private val threeDp = DecimalFormat("#,##0.###", symbols)
    private val whole = DecimalFormat("#,##0", symbols)

    fun number(value: Double, maxFractionDigits: Int = 2): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        val formatter = when (maxFractionDigits) {
            0 -> whole
            3 -> threeDp
            else -> twoDp
        }
        // Treat -0 as 0 to avoid odd "-0" output.
        val v = if (abs(value) < 1e-9) 0.0 else value
        return formatter.format(v)
    }

    fun integer(value: Int): String = whole.format(value.toLong())

    fun percent(fraction: Double): String =
        DecimalFormat("0.#%", symbols).format(fraction)

    fun money(value: Double): String = "\$${number(value, 2)}"

    /** Format a length in millimetres into the requested unit + label. */
    fun length(mm: Double, unitLabel: String, value: Double): String =
        "${number(value)} $unitLabel"

    /** "1 ft 3 1/2 in" style for imperial output. Used by the unit converter. */
    fun feetInches(mm: Double): String {
        val totalInches = mm / 25.4
        val feet = (totalInches / 12.0).toInt()
        val inches = totalInches - feet * 12.0
        return if (feet > 0) {
            "$feet ft ${number(inches, 2)} in"
        } else {
            "${number(inches, 2)} in"
        }
    }
}
