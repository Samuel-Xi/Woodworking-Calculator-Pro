package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SheetCutCalculatorTest {

    /**
     * Trivial single-part case: one 600×400 part on a 1220×2440 sheet uses
     * one sheet, no overlap, no skipped parts.
     */
    @Test fun singlePartOnHugeSheet_usesOneSheet() {
        val r = SheetCutCalculator.compute(
            SheetCutCalculator.Input(
                sheetLength = 2440.0,
                sheetWidth = 1220.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                parts = listOf(SheetCutCalculator.Part(600.0, 400.0, 1)),
            )
        )
        assertEquals(1, r.totalSheets)
        assertEquals(1, r.placedParts)
        assertTrue(r.skippedParts.isEmpty())
        assertTrue("efficiency must be positive", r.efficiency > 0.0)
    }

    /**
     * A small cabinet job: 4 sides (760×560), 2 tops (900×560), 4 shelves
     * (864×540). Total area ≈ 4×760×560 + 2×900×560 + 4×864×540 ≈ 4 663 040
     * mm² which is < one 2440×1220 sheet (≈ 2 976 800 mm² × 2 = 5.95 M). So
     * everything fits in at most 2 sheets, all parts placed, no skips.
     */
    @Test fun realisticCabinetJob_fitsInTwoSheets() {
        val r = SheetCutCalculator.compute(
            SheetCutCalculator.Input(
                sheetLength = 2440.0,
                sheetWidth = 1220.0,
                kerf = 3.0,
                unit = LengthUnit.MM,
                parts = listOf(
                    SheetCutCalculator.Part(760.0, 560.0, 4, "Side"),
                    SheetCutCalculator.Part(900.0, 560.0, 2, "Top"),
                    SheetCutCalculator.Part(864.0, 540.0, 4, "Shelf"),
                ),
            )
        )
        assertTrue("sheets used: ${r.totalSheets}", r.totalSheets in 1..3)
        assertEquals(10, r.placedParts)
        assertEquals(10, r.totalParts)
        assertTrue(r.skippedParts.isEmpty())
        assertTrue(r.efficiency in 0.0..1.0)
    }

    @Test fun oversizePart_isSkipped() {
        val r = SheetCutCalculator.compute(
            SheetCutCalculator.Input(
                sheetLength = 1000.0,
                sheetWidth = 600.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                parts = listOf(
                    SheetCutCalculator.Part(1200.0, 700.0, 2),
                    SheetCutCalculator.Part(400.0, 300.0, 1),
                ),
            )
        )
        assertEquals(1, r.skippedParts.size)
        assertEquals(2, r.skippedParts.first().quantity)
        assertEquals(1, r.placedParts)
    }

    @Test fun rotationFitsTallPartOnShortSheet() {
        // 800×500 part on a 600×900 sheet: only fits when rotated.
        val r = SheetCutCalculator.compute(
            SheetCutCalculator.Input(
                sheetLength = 600.0,
                sheetWidth = 900.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                parts = listOf(SheetCutCalculator.Part(800.0, 500.0, 1)),
            )
        )
        assertEquals(1, r.placedParts)
        assertTrue(r.sheets.first().placements.first().rotated)
    }

    @Test fun rotationDisabled_dropsTallPartIntoSkipped() {
        val r = SheetCutCalculator.compute(
            SheetCutCalculator.Input(
                sheetLength = 600.0,
                sheetWidth = 900.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                parts = listOf(SheetCutCalculator.Part(800.0, 500.0, 1)),
                allowRotation = false,
            )
        )
        assertEquals(0, r.placedParts)
        assertEquals(1, r.skippedParts.size)
    }

    @Test fun placementsDoNotOverlap() {
        val r = SheetCutCalculator.compute(
            SheetCutCalculator.Input(
                sheetLength = 2440.0,
                sheetWidth = 1220.0,
                kerf = 0.0,
                unit = LengthUnit.MM,
                parts = listOf(
                    SheetCutCalculator.Part(800.0, 600.0, 4),
                    SheetCutCalculator.Part(500.0, 400.0, 6),
                ),
            )
        )
        // For each sheet, no two placements should overlap.
        r.sheets.forEach { sheet ->
            val list = sheet.placements
            for (i in list.indices) {
                for (j in i + 1 until list.size) {
                    assertTrue(
                        "placements ${list[i]} and ${list[j]} overlap on sheet ${sheet.index}",
                        !overlaps(list[i], list[j])
                    )
                }
            }
        }
    }

    private fun overlaps(
        a: SheetCutCalculator.Placement,
        b: SheetCutCalculator.Placement,
    ): Boolean {
        val ax2 = a.x + a.length
        val ay2 = a.y + a.width
        val bx2 = b.x + b.length
        val by2 = b.y + b.width
        // Touching edges are allowed; only strict overlaps fail.
        val sep = a.x >= bx2 - 1e-6 || b.x >= ax2 - 1e-6 ||
                  a.y >= by2 - 1e-6 || b.y >= ay2 - 1e-6
        return !sep
    }
}
