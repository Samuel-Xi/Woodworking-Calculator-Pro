package com.woodworking.calculatorpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FractionTest {

    @Test fun plainDecimal_isParsedAsNonImperial() {
        val p = Fraction.parse("1.5")!!
        assertEquals(1.5, p.value, 1e-9)
        assertEquals(false, p.isImperial)
    }

    @Test fun europeanCommaDecimal_isAccepted() {
        val p = Fraction.parse("1,5")!!
        assertEquals(1.5, p.value, 1e-9)
    }

    @Test fun pureFraction_returnsRatio() {
        val p = Fraction.parse("1/2")!!
        assertEquals(0.5, p.value, 1e-9)
        assertEquals(false, p.isImperial)
    }

    @Test fun mixedNumber_spaceSeparated_returnsSum() {
        val p = Fraction.parse("3 1/8")!!
        assertEquals(3.125, p.value, 1e-9)
        assertEquals(false, p.isImperial)
    }

    @Test fun mixedNumber_hyphenSeparated_returnsSum() {
        val p = Fraction.parse("3-1/8")!!
        assertEquals(3.125, p.value, 1e-9)
    }

    @Test fun inchMark_alone_isImperial() {
        val p = Fraction.parse("6\"")!!
        assertEquals(6.0, p.value, 1e-9)
        assertTrue(p.isImperial)
    }

    @Test fun feetMark_aloneConvertsToInches() {
        val p = Fraction.parse("5'")!!
        assertEquals(60.0, p.value, 1e-9)
        assertTrue(p.isImperial)
    }

    @Test fun feetAndInches_sumsInInches() {
        val p = Fraction.parse("5' 6\"")!!
        assertEquals(66.0, p.value, 1e-9)
        assertTrue(p.isImperial)
    }

    @Test fun feetAndMixedInches_sumsCorrectly() {
        val p = Fraction.parse("5' 6 1/2\"")!!
        assertEquals(66.5, p.value, 1e-9)
        assertTrue(p.isImperial)
    }

    @Test fun feetAndMixedInches_compactNoSpaces_sumsCorrectly() {
        val p = Fraction.parse("5'6-1/2\"")!!
        assertEquals(66.5, p.value, 1e-9)
        assertTrue(p.isImperial)
    }

    @Test fun curlyQuotes_areNormalised() {
        val p = Fraction.parse("5\u2019 6\u201D")!!  // 5’ 6”
        assertEquals(66.0, p.value, 1e-9)
        assertTrue(p.isImperial)
    }

    @Test fun primeMarks_areNormalised() {
        val p = Fraction.parse("5\u2032 6\u2033")!!  // 5′ 6″
        assertEquals(66.0, p.value, 1e-9)
        assertTrue(p.isImperial)
    }

    @Test fun emptyAndBlank_returnNull() {
        assertNull(Fraction.parse(""))
        assertNull(Fraction.parse("   "))
    }

    @Test fun gibberish_returnsNull() {
        assertNull(Fraction.parse("abc"))
        assertNull(Fraction.parse("1//2"))
        assertNull(Fraction.parse("1/0"))
    }

    @Test fun parseValue_isShorthandForValue() {
        assertEquals(3.125, Fraction.parseValue("3 1/8"))
        assertNull(Fraction.parseValue(""))
    }
}
