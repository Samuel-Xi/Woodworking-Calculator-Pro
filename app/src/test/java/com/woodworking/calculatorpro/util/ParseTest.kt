package com.woodworking.calculatorpro.util

import com.woodworking.calculatorpro.domain.LengthUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParseTest {

    @Test fun parseDoubleOrNull_acceptsDecimal() {
        assertEquals(1.5, "1.5".parseDoubleOrNull())
    }

    @Test fun parseDoubleOrNull_acceptsFraction() {
        assertEquals(0.5, "1/2".parseDoubleOrNull())
        assertEquals(3.125, "3 1/8".parseDoubleOrNull())
    }

    @Test fun parseDoubleOrNull_blankReturnsNull() {
        assertNull("".parseDoubleOrNull())
        assertNull("   ".parseDoubleOrNull())
    }

    @Test fun parseIntOrNull_basic() {
        assertEquals(7, "7".parseIntOrNull())
        assertEquals(null, "".parseIntOrNull())
        assertEquals(null, "1.5".parseIntOrNull())
    }

    /**
     * When a user types feet/inch markers, the value must come back in the
     * field's working unit. "5' 6\"" entered while the field is in millimetres
     * should produce 1676.4 mm.
     */
    @Test fun parseLengthInUnit_convertsImperialMarkersToWorkingUnit() {
        val mm = "5' 6\"".parseLengthInUnit(LengthUnit.MM)!!
        assertEquals(1676.4, mm, 1e-6)
    }

    @Test fun parseLengthInUnit_passesThroughNonImperial() {
        // No feet/inch marks → the number is in the field's working unit as-is.
        val asInches = "3.5".parseLengthInUnit(LengthUnit.IN)!!
        assertEquals(3.5, asInches, 1e-9)
        val asMm = "150".parseLengthInUnit(LengthUnit.MM)!!
        assertEquals(150.0, asMm, 1e-9)
    }

    @Test fun parseLengthInUnit_imperialIntoInches_isIdentity() {
        val inches = "5' 6\"".parseLengthInUnit(LengthUnit.IN)!!
        assertEquals(66.0, inches, 1e-9)
    }
}
