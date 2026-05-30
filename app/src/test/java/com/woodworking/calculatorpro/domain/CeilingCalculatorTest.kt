package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CeilingCalculatorTest {

    /**
     * 4 m × 5 m room, joists every 400 mm across the 4 m width:
     * floor(4000 / 400) + 1 = 11 joists × 5 m = 55 m total length.
     */
    @Test fun typicalRoom_yieldsExpectedJoistCount() {
        val r = CeilingCalculator.compute(
            CeilingCalculator.Input(
                roomLength = 5.0,
                roomWidth = 4.0,
                spacing = 0.4,
                unit = LengthUnit.M,
                crossRunnerSpacing = 0.0,
            )
        )
        assertEquals(11, r.joistCount)
        assertEquals(55_000.0, r.joistLinearMm, 1e-9)
        assertEquals(0, r.crossCount)
    }

    @Test fun crossRunnersAddSecondAxis() {
        val r = CeilingCalculator.compute(
            CeilingCalculator.Input(
                roomLength = 5.0,
                roomWidth = 4.0,
                spacing = 0.4,
                unit = LengthUnit.M,
                crossRunnerSpacing = 1.0,
            )
        )
        // floor(5 / 1) + 1 = 6 cross runners × 4 m = 24 m.
        assertEquals(6, r.crossCount)
        assertEquals(24_000.0, r.crossLinearMm, 1e-9)
    }

    @Test fun zeroSpacing_returnsEmptyResult() {
        val r = CeilingCalculator.compute(
            CeilingCalculator.Input(
                roomLength = 5.0,
                roomWidth = 4.0,
                spacing = 0.0,
                unit = LengthUnit.M,
            )
        )
        assertEquals(0, r.joistCount)
        assertEquals(0.0, r.joistLinearMm, 1e-9)
    }
}
