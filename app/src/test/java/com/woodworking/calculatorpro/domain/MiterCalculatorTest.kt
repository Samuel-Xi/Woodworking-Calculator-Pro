package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Regression tests for [MiterCalculator]. These pin down the formulas a
 * carpenter actually relies on; a 1° drift in any of them ships a defect.
 */
class MiterCalculatorTest {

    @Test fun cornerBladeAngle_squareCorner_isFortyFive() {
        assertEquals(45.0, MiterCalculator.cornerBladeAngle(90.0), 1e-9)
    }

    @Test fun cornerBladeAngle_acuteCorner_isLargerCut() {
        // 60° interior → (180 - 60) / 2 = 60° saw setting.
        assertEquals(60.0, MiterCalculator.cornerBladeAngle(60.0), 1e-9)
    }

    @Test fun cornerBladeAngle_obtuseCorner_isSmallerCut() {
        // 120° interior → 30° saw setting.
        assertEquals(30.0, MiterCalculator.cornerBladeAngle(120.0), 1e-9)
    }

    @Test fun cornerBladeAngle_outsideRange_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            MiterCalculator.cornerBladeAngle(-5.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MiterCalculator.cornerBladeAngle(181.0)
        }
    }

    @Test fun polygonBladeAngle_pictureFrame_isFortyFive() {
        assertEquals(45.0, MiterCalculator.polygonBladeAngle(4), 1e-9)
    }

    @Test fun polygonBladeAngle_hexagon_isThirty() {
        assertEquals(30.0, MiterCalculator.polygonBladeAngle(6), 1e-9)
    }

    @Test fun polygonBladeAngle_minSides_isThree() {
        // Equilateral triangle picture frame → 60° cuts.
        assertEquals(60.0, MiterCalculator.polygonBladeAngle(3), 1e-9)
    }

    @Test fun polygonBladeAngle_invalidSides_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            MiterCalculator.polygonBladeAngle(2)
        }
    }

    @Test fun polygonInteriorAngle_square_isNinety() {
        assertEquals(90.0, MiterCalculator.polygonInteriorAngle(4), 1e-9)
    }

    @Test fun polygonInteriorAngle_hexagon_isOneTwenty() {
        assertEquals(120.0, MiterCalculator.polygonInteriorAngle(6), 1e-9)
    }

    /**
     * 38° spring crown moulding into a 90° corner. The classical reference
     * values are blade ≈ 31.62° and bevel ≈ 33.86° — these are the numbers
     * printed on the back of most consumer crown moulding boxes.
     */
    @Test fun compound_38SpringNinetyWall_matchesReference() {
        val r = MiterCalculator.compound(springAngleDeg = 38.0, wallCornerDeg = 90.0)
        assertEquals(31.62, r.bladeAngleDeg, 0.02)
        assertEquals(33.86, r.bevelAngleDeg, 0.02)
    }

    @Test fun compound_45SpringNinetyWall_matchesReference() {
        // 45° spring × 90° wall → blade ≈ 30°, bevel ≈ 30°.
        val r = MiterCalculator.compound(springAngleDeg = 45.0, wallCornerDeg = 90.0)
        assertEquals(30.0, r.bladeAngleDeg, 0.02)
        assertEquals(30.0, r.bevelAngleDeg, 0.02)
    }

    @Test fun compound_zeroSpring_isPlainMiter() {
        // No spring → behaves exactly like a flat corner cut.
        val r = MiterCalculator.compound(springAngleDeg = 0.0, wallCornerDeg = 90.0)
        assertEquals(45.0, r.bladeAngleDeg, 1e-9)
        assertEquals(0.0, r.bevelAngleDeg, 1e-9)
    }

    @Test fun compound_invalidSpring_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            MiterCalculator.compound(springAngleDeg = 91.0, wallCornerDeg = 90.0)
        }
    }
}
