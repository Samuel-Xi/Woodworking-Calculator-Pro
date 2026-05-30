package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StairCalculatorTest {

    /**
     * Classic interior stair: 108" total rise (typical 9 ft floor-to-floor),
     * 108" total run, 7" preferred riser. Should give 15 risers / 14 treads,
     * 7.20" risers and ~7.71" treads. Treads come out below the 10" IRC
     * minimum, so the geometry is flagged as out-of-code.
     */
    @Test fun residentialNineFootRun_isWithinExpectedCounts() {
        val r = StairCalculator.compute(
            StairCalculator.Input(
                totalRise = 108.0,
                totalRun = 108.0,
                preferredRiser = 7.0,
                unit = LengthUnit.IN,
            )
        )
        assertEquals(15, r.numRisers)
        assertEquals(14, r.numTreads)
        // Convert riser back to inches for the assertion.
        val riserIn = convertLength(r.riserHeightMm, LengthUnit.MM, LengthUnit.IN)
        assertEquals(7.20, riserIn, 0.01)
        // 14 treads spread across 108" → ~7.71" treads. Out of IRC code.
        val treadIn = convertLength(r.treadDepthMm, LengthUnit.MM, LengthUnit.IN)
        assertEquals(7.71, treadIn, 0.01)
        assertFalse(r.withinCode)
    }

    @Test fun pitchAndStringer_matchPythagoras() {
        // 7' rise, 10' run → stringer = √(7² + 10²) ft = √149 ≈ 12.207 ft.
        val r = StairCalculator.compute(
            StairCalculator.Input(
                totalRise = 7.0,
                totalRun = 10.0,
                preferredRiser = 7.5 / 12.0, // 7.5 inches in feet
                unit = LengthUnit.FT,
            )
        )
        val stringerFt = convertLength(r.stringerLengthMm, LengthUnit.MM, LengthUnit.FT)
        assertEquals(12.207, stringerFt, 0.01)
        assertEquals(34.99, r.pitchDeg, 0.05)
    }

    @Test fun zeroRise_returnsEmptyResult() {
        val r = StairCalculator.compute(
            StairCalculator.Input(0.0, 0.0, 7.0, LengthUnit.IN)
        )
        assertEquals(0, r.numRisers)
        assertEquals(0, r.numTreads)
        assertFalse(r.withinCode)
    }

    @Test fun codeCompliantConfiguration_isReportedOk() {
        // 105" rise, 10" treads, 7" riser preference. 15 risers (7.0" each)
        // 14 treads at ~140" run → 10" each. Within IRC.
        val r = StairCalculator.compute(
            StairCalculator.Input(
                totalRise = 105.0,
                totalRun = 140.0,
                preferredRiser = 7.0,
                unit = LengthUnit.IN,
            )
        )
        val riserIn = convertLength(r.riserHeightMm, LengthUnit.MM, LengthUnit.IN)
        val treadIn = convertLength(r.treadDepthMm, LengthUnit.MM, LengthUnit.IN)
        assertEquals(7.0, riserIn, 0.01)
        assertEquals(10.0, treadIn, 0.01)
        assertTrue(r.withinCode)
    }
}
