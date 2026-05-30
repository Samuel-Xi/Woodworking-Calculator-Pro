package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlooringCalculatorTest {

    /**
     * A 4 m × 5 m room with 1.2 m × 0.2 m planks → 20 m² floor area, 0.24 m²
     * per plank → ceil(83.33) = 84 base pieces.
     */
    @Test fun fourByFiveRoom_withMetricPlanks_countsCorrectly() {
        val r = FlooringCalculator.compute(
            FlooringCalculator.Input(
                roomLength = 5.0, roomWidth = 4.0, roomUnit = LengthUnit.M,
                plankLength = 1.2, plankWidth = 0.2, plankUnit = LengthUnit.M,
                wastePercent = 0.10,
                piecesPerBox = null,
            )
        )
        assertEquals(20.0, r.roomAreaM2, 1e-9)
        assertEquals(0.24, r.pieceAreaM2, 1e-9)
        assertEquals(84, r.piecesBase)
        // 84 × 1.10 = 92.4 → ceil → 93.
        assertEquals(93, r.piecesWithWaste)
        assertNull(r.boxesNeeded)
    }

    @Test fun boxCount_roundsUpAfterWaste() {
        val r = FlooringCalculator.compute(
            FlooringCalculator.Input(
                roomLength = 5.0, roomWidth = 4.0, roomUnit = LengthUnit.M,
                plankLength = 1.2, plankWidth = 0.2, plankUnit = LengthUnit.M,
                wastePercent = 0.10,
                piecesPerBox = 8,
            )
        )
        // 93 pieces / 8 per box = 11.625 → 12 boxes.
        assertEquals(12, r.boxesNeeded)
    }

    @Test fun zeroDimension_returnsZeroPieces() {
        val r = FlooringCalculator.compute(
            FlooringCalculator.Input(
                roomLength = 0.0, roomWidth = 4.0, roomUnit = LengthUnit.M,
                plankLength = 1.2, plankWidth = 0.2, plankUnit = LengthUnit.M,
                wastePercent = 0.10,
                piecesPerBox = 8,
            )
        )
        assertEquals(0, r.piecesBase)
        assertEquals(0, r.piecesWithWaste)
    }

    @Test fun wasteClampedAtHundredPercent() {
        // Pass an absurd 5.0 (500%) — it should clamp to 100%, doubling the count.
        val r = FlooringCalculator.compute(
            FlooringCalculator.Input(
                roomLength = 5.0, roomWidth = 4.0, roomUnit = LengthUnit.M,
                plankLength = 1.2, plankWidth = 0.2, plankUnit = LengthUnit.M,
                wastePercent = 5.0,
                piecesPerBox = null,
            )
        )
        assertEquals(168, r.piecesWithWaste) // 84 × 2.
    }
}
