package com.woodworking.calculatorpro.domain

import kotlin.math.max

object BoardFootCalculator {
    data class Input(
        val thicknessIn: Double,
        val widthIn: Double,
        val lengthFt: Double,
        val quantity: Int,
        val wastePercent: Double,
        val pricePerBoardFoot: Double?,
    )

    data class Result(
        val perPieceBoardFeet: Double,
        val baseBoardFeet: Double,
        val wasteBoardFeet: Double,
        val totalBoardFeet: Double,
        val estimatedCost: Double?,
    )

    fun compute(input: Input): Result {
        val qty = max(1, input.quantity)
        val perPiece = input.thicknessIn * input.widthIn * input.lengthFt / 12.0
        val base = perPiece * qty
        val wasteFraction = max(0.0, input.wastePercent) / 100.0
        val waste = base * wasteFraction
        val total = base + waste
        val cost = input.pricePerBoardFoot?.takeIf { it >= 0.0 }?.let { it * total }
        return Result(perPiece, base, waste, total, cost)
    }
}
