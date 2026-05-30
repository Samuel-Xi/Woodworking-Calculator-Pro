package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BoardFootCalculatorTest {

    /**
     * Reference: a single 1×6×8' board is 4 bd ft
     * (1 × 6 × 8 / 12 = 4). With six pieces that's 24 bd ft base, 10% waste
     * brings it to 26.4, and at $4.25/bd ft the cost is $112.20.
     */
    @Test fun sixOneBySixEightFoot_withTenPercentWaste_isReferenceAccurate() {
        val r = BoardFootCalculator.compute(
            BoardFootCalculator.Input(
                thicknessIn = 1.0,
                widthIn = 6.0,
                lengthFt = 8.0,
                quantity = 6,
                wastePercent = 10.0,
                pricePerBoardFoot = 4.25,
            )
        )
        assertEquals(4.0, r.perPieceBoardFeet, 1e-9)
        assertEquals(24.0, r.baseBoardFeet, 1e-9)
        assertEquals(2.4, r.wasteBoardFeet, 1e-9)
        assertEquals(26.4, r.totalBoardFeet, 1e-9)
        assertEquals(112.2, r.estimatedCost!!, 1e-9)
    }

    @Test fun noPriceProvided_returnsNullCost() {
        val r = BoardFootCalculator.compute(
            BoardFootCalculator.Input(
                thicknessIn = 1.5,
                widthIn = 5.5,
                lengthFt = 10.0,
                quantity = 1,
                wastePercent = 0.0,
                pricePerBoardFoot = null,
            )
        )
        // 1.5 × 5.5 × 10 / 12 = 6.875 bd ft.
        assertEquals(6.875, r.totalBoardFeet, 1e-9)
        assertNull(r.estimatedCost)
    }

    @Test fun zeroQuantity_isTreatedAsOne() {
        val r = BoardFootCalculator.compute(
            BoardFootCalculator.Input(
                thicknessIn = 1.0,
                widthIn = 4.0,
                lengthFt = 8.0,
                quantity = 0,
                wastePercent = 0.0,
                pricePerBoardFoot = null,
            )
        )
        // 1 × 4 × 8 / 12 = 2.666... ; quantity floored to 1.
        assertEquals(2.6666667, r.totalBoardFeet, 1e-6)
    }

    @Test fun negativeWaste_isClampedToZero() {
        val r = BoardFootCalculator.compute(
            BoardFootCalculator.Input(
                thicknessIn = 1.0,
                widthIn = 4.0,
                lengthFt = 8.0,
                quantity = 3,
                wastePercent = -25.0,
                pricePerBoardFoot = null,
            )
        )
        assertEquals(0.0, r.wasteBoardFeet, 1e-9)
        assertEquals(r.baseBoardFeet, r.totalBoardFeet, 1e-9)
    }
}
