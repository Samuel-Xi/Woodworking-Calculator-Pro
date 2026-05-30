package com.woodworking.calculatorpro.domain

import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import kotlin.math.PI

/**
 * Miter angle calculators. All inputs/outputs are expressed in degrees.
 *
 * Three modes are supported:
 * - Corner:   the interior angle of the joint is known → blade angle = (180 - θ) / 2
 * - Polygon:  for regular N-sided frames → blade angle = 180 / N
 * - Compound: crown moulding with a given spring angle against a non-90° wall;
 *             returns both blade (miter) angle and bevel (tilt) angle.
 */
object MiterCalculator {

    /**
     * Simple corner miter. Example: a 90° frame corner yields a 45° blade cut.
     * Inside corners > 180° are rejected (angle must be strictly between 0 and 180).
     */
    fun cornerBladeAngle(insideCornerDeg: Double): Double {
        require(insideCornerDeg in 0.0..180.0) { "Corner angle out of range" }
        return (180.0 - insideCornerDeg) / 2.0
    }

    /**
     * Regular polygon miter. N ≥ 3. Each blade cut equals 180° / N.
     * E.g. hexagon picture frame → 30° cuts.
     */
    fun polygonBladeAngle(sides: Int): Double {
        require(sides >= 3) { "Polygon must have at least 3 sides" }
        return 180.0 / sides
    }

    /** Interior angle of a regular polygon — useful to display alongside results. */
    fun polygonInteriorAngle(sides: Int): Double {
        require(sides >= 3)
        return (sides - 2) * 180.0 / sides
    }

    data class CompoundResult(val bladeAngleDeg: Double, val bevelAngleDeg: Double)

    /**
     * Compound miter for crown moulding. Derived from the classic formulas
     * described in the Woodworker's Journal and used by most saw manuals.
     *
     *  tan(blade) = cos(spring) · tan(wall / 2)
     *  sin(bevel) = sin(spring) · cos(wall / 2)
     *
     * @param springAngleDeg angle the crown makes with the wall (e.g. 38° or 45°)
     * @param wallCornerDeg  the corner's interior angle (90° for most rooms)
     */
    fun compound(springAngleDeg: Double, wallCornerDeg: Double): CompoundResult {
        require(springAngleDeg in 0.0..90.0) { "Spring angle out of range" }
        require(wallCornerDeg in 0.0..180.0) { "Wall angle out of range" }

        val spring = springAngleDeg.toRad()
        val halfWall = (wallCornerDeg / 2.0).toRad()

        val blade = atan(cos(spring) * tan(halfWall)).toDeg()
        val bevelArg = (sin(spring) * cos(halfWall)).coerceIn(-1.0, 1.0)
        val bevel = kotlin.math.asin(bevelArg).toDeg()
        return CompoundResult(blade, bevel)
    }

    private fun Double.toRad() = this * PI / 180.0
    private fun Double.toDeg() = this * 180.0 / PI
}
