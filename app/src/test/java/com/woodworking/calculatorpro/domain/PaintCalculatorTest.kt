package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PaintCalculatorTest {

    @Test fun metric_twoCoats_subtractsOpenings() {
        // 12 m wall, 2.7 m tall = 32.4 m² gross.
        // 4 m² of doors+windows → 28.4 m² net.
        // 28.4 × 2 coats / 10 m² per L = 5.68 L.
        val r = PaintCalculator.compute(
            PaintCalculator.Input(
                system = PaintCalculator.System.METRIC,
                wallLength = 12.0,
                wallHeight = 2.7,
                openings = 4.0,
                coverage = 10.0,
                coats = 2,
            )
        )
        assertEquals(28.4, r.netArea, 1e-9)
        assertEquals(5.68, r.totalVolume, 1e-9)
        assertEquals("m²", r.areaUnitLabel)
        assertEquals("L", r.volumeUnitLabel)
    }

    @Test fun imperial_oneCoat_usesGallons() {
        // 40 ft × 8 ft = 320 ft², no openings, 350 ft²/gal → 0.914 gal.
        val r = PaintCalculator.compute(
            PaintCalculator.Input(
                system = PaintCalculator.System.IMPERIAL,
                wallLength = 40.0,
                wallHeight = 8.0,
                openings = 0.0,
                coverage = 350.0,
                coats = 1,
            )
        )
        assertEquals(320.0, r.netArea, 1e-9)
        assertEquals(0.9142857, r.totalVolume, 1e-6)
        assertEquals("gal", r.volumeUnitLabel)
    }

    @Test fun zeroCoverage_returnsZero() {
        val r = PaintCalculator.compute(
            PaintCalculator.Input(
                system = PaintCalculator.System.METRIC,
                wallLength = 5.0,
                wallHeight = 3.0,
                openings = 0.0,
                coverage = 0.0,
                coats = 2,
            )
        )
        assertEquals(0.0, r.totalVolume, 1e-9)
    }

    @Test fun openingsLargerThanWall_clampToZero() {
        val r = PaintCalculator.compute(
            PaintCalculator.Input(
                system = PaintCalculator.System.METRIC,
                wallLength = 2.0,
                wallHeight = 2.0,
                openings = 100.0,
                coverage = 10.0,
                coats = 1,
            )
        )
        assertEquals(0.0, r.netArea, 1e-9)
    }

    @Test fun roundUpToWholeUnit_alwaysRoundsUp() {
        assertEquals(1, PaintCalculator.roundUpToWholeUnit(0.1))
        assertEquals(6, PaintCalculator.roundUpToWholeUnit(5.01))
        assertEquals(5, PaintCalculator.roundUpToWholeUnit(5.0))
    }
}
