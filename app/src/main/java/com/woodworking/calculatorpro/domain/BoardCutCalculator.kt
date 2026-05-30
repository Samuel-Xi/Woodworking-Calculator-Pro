package com.woodworking.calculatorpro.domain

/**
 * One-dimensional cutting-stock optimiser. Uses First-Fit-Decreasing (FFD),
 * a fast deterministic heuristic that gives near-optimal layouts for typical
 * woodworking cut lists.
 *
 * Algorithm:
 *   1. Expand every (length, qty) pair into individual cuts.
 *   2. Sort cuts in descending order.
 *   3. For each cut, place it on the first stock with enough room
 *      (remaining ≥ cut + kerf when other cuts are already there);
 *      if none fits, open a new stock.
 *   4. Return the per-stock layout, total stocks used and waste.
 */
object BoardCutCalculator {

    data class CutRequest(val length: Double, val quantity: Int)

    data class Stock(val index: Int, val cuts: List<Double>, val remaining: Double)

    data class Input(
        val stockLength: Double,
        val kerf: Double,
        val unit: LengthUnit,
        val cuts: List<CutRequest>,
    )

    data class Result(
        val stocks: List<Stock>,
        val totalStocks: Int,
        val totalCuts: Int,
        val totalWasteMm: Double,
        val efficiency: Double, // 0..1
        val infeasibleCuts: List<Double>, // cuts longer than the stock
        val unit: LengthUnit,
    )

    fun compute(input: Input): Result {
        val stockMm = convertLength(input.stockLength, input.unit, LengthUnit.MM)
        val kerfMm  = convertLength(input.kerf,        input.unit, LengthUnit.MM).coerceAtLeast(0.0)

        // Flatten and validate.
        val infeasibleMm = mutableListOf<Double>()
        val cutsMm = mutableListOf<Double>()
        for (req in input.cuts) {
            if (req.quantity <= 0 || req.length <= 0) continue
            val mm = convertLength(req.length, input.unit, LengthUnit.MM)
            if (mm > stockMm) {
                repeat(req.quantity) { infeasibleMm += mm }
            } else {
                repeat(req.quantity) { cutsMm += mm }
            }
        }

        if (stockMm <= 0.0 || cutsMm.isEmpty()) {
            return Result(
                stocks = emptyList(),
                totalStocks = 0,
                totalCuts = 0,
                totalWasteMm = 0.0,
                efficiency = 0.0,
                infeasibleCuts = infeasibleMm.map { convertLength(it, LengthUnit.MM, input.unit) },
                unit = input.unit,
            )
        }

        cutsMm.sortDescending()

        // FFD packing.
        data class Bin(val cuts: MutableList<Double> = mutableListOf(), var used: Double = 0.0)
        val bins = mutableListOf<Bin>()
        for (cut in cutsMm) {
            val bin = bins.firstOrNull { existing ->
                val needed = if (existing.cuts.isEmpty()) cut else existing.used + kerfMm + cut
                needed <= stockMm + 1e-6
            }
            if (bin == null) {
                val nb = Bin()
                nb.cuts += cut
                nb.used = cut
                bins += nb
            } else {
                if (bin.cuts.isEmpty()) {
                    bin.cuts += cut
                    bin.used = cut
                } else {
                    bin.cuts += cut
                    bin.used += kerfMm + cut
                }
            }
        }

        val stocks = bins.mapIndexed { i, b ->
            Stock(
                index = i + 1,
                cuts = b.cuts.map { convertLength(it, LengthUnit.MM, input.unit) },
                remaining = convertLength(stockMm - b.used, LengthUnit.MM, input.unit),
            )
        }

        val totalUsed = bins.sumOf { it.used }
        val totalLength = bins.size * stockMm
        val waste = (totalLength - totalUsed).coerceAtLeast(0.0)
        val efficiency = if (totalLength > 0) totalUsed / totalLength else 0.0

        return Result(
            stocks = stocks,
            totalStocks = bins.size,
            totalCuts = cutsMm.size,
            totalWasteMm = waste,
            efficiency = efficiency,
            infeasibleCuts = infeasibleMm.map { convertLength(it, LengthUnit.MM, input.unit) },
            unit = input.unit,
        )
    }
}
