package com.woodworking.calculatorpro.domain

import kotlin.math.ceil

/**
 * Floor / tile usage estimator. All lengths are converted internally to metres
 * so area arithmetic uses SI units; the UI is responsible for localising back.
 */
object FlooringCalculator {

    data class Input(
        val roomLength: Double,
        val roomWidth: Double,
        val roomUnit: LengthUnit,
        val plankLength: Double,
        val plankWidth: Double,
        val plankUnit: LengthUnit,
        /** 0.0 to 1.0 — e.g. 0.10 for 10% waste. */
        val wastePercent: Double,
        /** Optional. 0 or null disables box-count reporting. */
        val piecesPerBox: Int?,
    )

    data class Result(
        val roomAreaM2: Double,
        val pieceAreaM2: Double,
        val piecesBase: Int,
        val piecesWithWaste: Int,
        val boxesNeeded: Int?,
    )

    fun compute(input: Input): Result {
        val lengthM = convertLength(input.roomLength,  input.roomUnit, LengthUnit.MM) / 1000.0
        val widthM  = convertLength(input.roomWidth,   input.roomUnit, LengthUnit.MM) / 1000.0
        val pLenM   = convertLength(input.plankLength, input.plankUnit, LengthUnit.MM) / 1000.0
        val pWidM   = convertLength(input.plankWidth,  input.plankUnit, LengthUnit.MM) / 1000.0

        val roomArea = (lengthM * widthM).coerceAtLeast(0.0)
        val pieceArea = (pLenM * pWidM).coerceAtLeast(0.0)

        if (pieceArea <= 0.0 || roomArea <= 0.0) {
            return Result(roomArea, pieceArea, 0, 0, input.piecesPerBox?.let { 0 })
        }

        val baseCount = ceil(roomArea / pieceArea).toInt()
        val waste = input.wastePercent.coerceIn(0.0, 1.0)
        val withWaste = ceil(baseCount * (1.0 + waste)).toInt()

        val boxes = input.piecesPerBox?.takeIf { it > 0 }?.let { ppb ->
            ceil(withWaste.toDouble() / ppb).toInt()
        }
        return Result(roomArea, pieceArea, baseCount, withWaste, boxes)
    }
}
