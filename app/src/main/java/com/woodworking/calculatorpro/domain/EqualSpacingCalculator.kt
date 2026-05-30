package com.woodworking.calculatorpro.domain

import kotlin.math.ceil
import kotlin.math.max

object EqualSpacingCalculator {
    enum class Mode { KNOWN_COUNT, TARGET_GAP }

    data class Input(
        val mode: Mode,
        val span: Double,
        val itemWidth: Double,
        val itemCount: Int?,
        val targetGap: Double?,
    )

    data class Result(
        val itemCount: Int,
        val gap: Double,
        val centerToCenter: Double,
        val firstCenter: Double,
        val positions: List<Double>,
        val valid: Boolean,
    )

    fun compute(input: Input): Result? {
        if (input.span <= 0.0 || input.itemWidth < 0.0) return null
        val count = when (input.mode) {
            Mode.KNOWN_COUNT -> input.itemCount?.takeIf { it > 0 } ?: return null
            Mode.TARGET_GAP -> {
                val target = input.targetGap?.takeIf { it > 0.0 } ?: return null
                max(1, ceil((input.span - target) / (input.itemWidth + target)).toInt())
            }
        }
        val usable = input.span - input.itemWidth * count
        val gap = usable / (count + 1)
        val centerToCenter = input.itemWidth + gap
        val firstCenter = gap + input.itemWidth / 2.0
        val positions = List(count) { index -> firstCenter + index * centerToCenter }
        return Result(
            itemCount = count,
            gap = gap,
            centerToCenter = centerToCenter,
            firstCenter = firstCenter,
            positions = positions,
            valid = gap >= 0.0,
        )
    }
}
