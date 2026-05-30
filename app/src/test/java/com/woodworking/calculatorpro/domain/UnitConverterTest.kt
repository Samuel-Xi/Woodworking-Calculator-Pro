package com.woodworking.calculatorpro.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test fun inchToMm_isExact() {
        assertEquals(25.4, convertLength(1.0, LengthUnit.IN, LengthUnit.MM), 1e-9)
        assertEquals(304.8, convertLength(1.0, LengthUnit.FT, LengthUnit.MM), 1e-9)
    }

    @Test fun roundTrip_isIdentity() {
        val values = listOf(0.0, 1.0, 12.5, 4096.0)
        for (v in values) {
            val mm = convertLength(v, LengthUnit.IN, LengthUnit.MM)
            val back = convertLength(mm, LengthUnit.MM, LengthUnit.IN)
            assertEquals(v, back, 1e-9)
        }
    }

    @Test fun metresAndFeet() {
        // 1 m = 3.280839... ft.
        assertEquals(3.2808399, convertLength(1.0, LengthUnit.M, LengthUnit.FT), 1e-6)
        // 1 ft = 0.3048 m exactly.
        assertEquals(0.3048, convertLength(1.0, LengthUnit.FT, LengthUnit.M), 1e-9)
    }

    @Test fun areaUnits_roundTrip() {
        val originalFt2 = 500.0
        val m2 = convertArea(originalFt2, AreaUnit.FT2, AreaUnit.M2)
        val back = convertArea(m2, AreaUnit.M2, AreaUnit.FT2)
        assertEquals(originalFt2, back, 1e-6)
    }

    @Test fun gallonsToLitres() {
        // 1 US gallon = 3.7854118 L.
        assertEquals(3.7854118, VolumeConverter.galToLiters(1.0), 1e-6)
        assertEquals(1.0, VolumeConverter.litersToGal(VolumeConverter.galToLiters(1.0)), 1e-9)
    }
}
