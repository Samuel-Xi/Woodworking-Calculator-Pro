package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EqualSpacingCalculatorTest {

    /**
     * 96" span, 7 balusters of 1.5" width, known count:
     * usable = 96 - 7×1.5 = 85.5; 8 gaps → 10.6875" each.
     */
    @Test fun knownCount_distributesGapsEvenly() {
        val r = EqualSpacingCalculator.compute(
            EqualSpacingCalculator.Input(
                mode = EqualSpacingCalculator.Mode.KNOWN_COUNT,
                span = 96.0,
                itemWidth = 1.5,
                itemCount = 7,
                targetGap = null,
            )
        )
        assertNotNull(r)
        assertEquals(7, r!!.itemCount)
        assertEquals(10.6875, r.gap, 1e-9)
        assertEquals(7, r.positions.size)
        // First centre is at gap + width/2 = 10.6875 + 0.75 = 11.4375.
        assertEquals(11.4375, r.firstCenter, 1e-9)
        assertTrue(r.valid)
    }

    /**
     * 96" span, 1.5" item, target max gap = 4":
     * count = max(1, ceil((96 - 4)/(1.5 + 4))) = max(1, ceil(16.727)) = 17.
     * usable = 96 - 17×1.5 = 70.5; 18 gaps → 3.9166" each (below target — ok).
     */
    @Test fun targetGap_picksCountThatRespectsLimit() {
        val r = EqualSpacingCalculator.compute(
            EqualSpacingCalculator.Input(
                mode = EqualSpacingCalculator.Mode.TARGET_GAP,
                span = 96.0,
                itemWidth = 1.5,
                itemCount = null,
                targetGap = 4.0,
            )
        )
        assertNotNull(r)
        assertEquals(17, r!!.itemCount)
        assertTrue("gap (${r.gap}) must not exceed target", r.gap <= 4.0 + 1e-9)
    }

    @Test fun itemsTooWide_marksResultInvalid() {
        val r = EqualSpacingCalculator.compute(
            EqualSpacingCalculator.Input(
                mode = EqualSpacingCalculator.Mode.KNOWN_COUNT,
                span = 10.0,
                itemWidth = 3.0,
                itemCount = 5,
                targetGap = null,
            )
        )
        assertNotNull(r)
        // 10 - 5×3 = -5, gap = -1; not valid.
        assertTrue(r!!.gap < 0.0)
        assertEquals(false, r.valid)
    }

    @Test fun invalidInputs_returnNull() {
        val zeroSpan = EqualSpacingCalculator.compute(
            EqualSpacingCalculator.Input(
                mode = EqualSpacingCalculator.Mode.KNOWN_COUNT,
                span = 0.0,
                itemWidth = 1.0,
                itemCount = 5,
                targetGap = null,
            )
        )
        assertNull(zeroSpan)

        val missingTarget = EqualSpacingCalculator.compute(
            EqualSpacingCalculator.Input(
                mode = EqualSpacingCalculator.Mode.TARGET_GAP,
                span = 100.0,
                itemWidth = 1.0,
                itemCount = null,
                targetGap = null,
            )
        )
        assertNull(missingTarget)
    }
}
