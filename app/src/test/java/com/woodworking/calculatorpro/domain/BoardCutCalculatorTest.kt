package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardCutCalculatorTest {

    @Test fun singleStock_fitsAllCutsWithoutKerf() {
        // 8 ft stock, three 30" cuts (= 90" total) → 1 stock used.
        val r = BoardCutCalculator.compute(
            BoardCutCalculator.Input(
                stockLength = 96.0,
                kerf = 0.0,
                unit = LengthUnit.IN,
                cuts = listOf(BoardCutCalculator.CutRequest(30.0, 3)),
            )
        )
        assertEquals(1, r.totalStocks)
        assertEquals(3, r.totalCuts)
        assertEquals(1, r.stocks.size)
        assertEquals(3, r.stocks[0].cuts.size)
        r.stocks[0].cuts.forEach { assertEquals(30.0, it, 1e-6) }
        // 90" used of 96" → ~93.75%.
        assertEquals(0.9375, r.efficiency, 1e-6)
    }

    @Test fun kerfIsHonoured_betweenCuts() {
        // 100 mm stock, two 40 mm cuts with 5 mm kerf:
        // 40 + 5 + 40 = 85 mm used; everything still fits on one stock.
        val r = BoardCutCalculator.compute(
            BoardCutCalculator.Input(
                stockLength = 100.0,
                kerf = 5.0,
                unit = LengthUnit.MM,
                cuts = listOf(BoardCutCalculator.CutRequest(40.0, 2)),
            )
        )
        assertEquals(1, r.totalStocks)
        // 85 mm used / 100 mm = 0.85.
        assertEquals(0.85, r.efficiency, 1e-9)
    }

    @Test fun overflowSpills_intoSecondStock() {
        // 100 mm stock, three 40 mm cuts with 5 mm kerf:
        // First stock: 40 + 5 + 40 = 85 mm (one 40 mm cut won't fit on top).
        // Second stock: 40 mm alone.
        val r = BoardCutCalculator.compute(
            BoardCutCalculator.Input(
                stockLength = 100.0,
                kerf = 5.0,
                unit = LengthUnit.MM,
                cuts = listOf(BoardCutCalculator.CutRequest(40.0, 3)),
            )
        )
        assertEquals(2, r.totalStocks)
    }

    @Test fun cutsLongerThanStock_areReportedInfeasible() {
        val r = BoardCutCalculator.compute(
            BoardCutCalculator.Input(
                stockLength = 100.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                cuts = listOf(BoardCutCalculator.CutRequest(150.0, 2)),
            )
        )
        assertEquals(0, r.totalStocks)
        assertEquals(2, r.infeasibleCuts.size)
        assertTrue(r.infeasibleCuts.all { it >= 150.0 - 1e-6 })
    }

    @Test fun emptyInput_returnsZero() {
        val r = BoardCutCalculator.compute(
            BoardCutCalculator.Input(
                stockLength = 100.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                cuts = emptyList(),
            )
        )
        assertEquals(0, r.totalStocks)
        assertEquals(0, r.totalCuts)
        assertEquals(0.0, r.efficiency, 1e-9)
    }

    @Test fun packing_prefersLongCutsFirst_FFD() {
        // Three 70 mm + one 30 mm with 0 kerf on a 100 mm stock:
        // FFD packs (70+30) and (70) and (70) → 3 stocks.
        val r = BoardCutCalculator.compute(
            BoardCutCalculator.Input(
                stockLength = 100.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                cuts = listOf(
                    BoardCutCalculator.CutRequest(70.0, 3),
                    BoardCutCalculator.CutRequest(30.0, 1),
                ),
            )
        )
        assertEquals(3, r.totalStocks)
        // Total used: 70*3 + 30 = 240 mm out of 300 mm → 80%.
        assertEquals(0.80, r.efficiency, 1e-9)
    }
}
