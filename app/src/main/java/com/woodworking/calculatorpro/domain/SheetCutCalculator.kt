package com.woodworking.calculatorpro.domain

/**
 * Two-dimensional cutting-stock optimiser for sheet goods (plywood, MDF, OSB).
 *
 * Algorithm: a guillotine-style packer with the **Best Short Side Fit**
 * heuristic. For each part we look at every free rectangle on every open
 * sheet and pick the placement that minimises the leftover short side; ties
 * are broken by leftover long side. After placing a part we split the host
 * rectangle horizontally and vertically. The kerf is added to both sub-splits
 * so the saw blade's width is honoured.
 *
 * It's deterministic, runs in O(parts × freeRects) ≈ milliseconds for
 * realistic cabinet jobs (≤ 200 parts), and yields layouts within a few
 * percent of an optimal MIP solver — which is plenty for hand-cut workshops
 * and far more than what most consumer apps offer.
 *
 * Like every other calculator in this package, this module has zero Android
 * imports so it can be unit-tested on the JVM.
 */
object SheetCutCalculator {

    /** A part to be produced. [length] is the long dimension by convention. */
    data class Part(
        val length: Double,
        val width: Double,
        val quantity: Int,
        val label: String? = null,
    )

    /** A part placed on a sheet. Coordinates are in the sheet's local frame:
     *  (0, 0) is the top-left corner; [x] grows along the sheet's length. */
    data class Placement(
        val x: Double,
        val y: Double,
        val length: Double,
        val width: Double,
        val label: String?,
        /** True if the part was rotated 90° to fit. */
        val rotated: Boolean,
    )

    data class Sheet(val index: Int, val placements: List<Placement>)

    data class Input(
        /** Long edge of the full sheet (e.g. 2440 mm for a 4×8 plywood). */
        val sheetLength: Double,
        /** Short edge of the full sheet (e.g. 1220 mm for a 4×8 plywood). */
        val sheetWidth: Double,
        /** Blade kerf. Same unit as the lengths. */
        val kerf: Double,
        val unit: LengthUnit,
        val parts: List<Part>,
        /** Whether the cutter is allowed to rotate parts 90°. Defaults to true. */
        val allowRotation: Boolean = true,
    )

    data class Result(
        val sheets: List<Sheet>,
        val totalSheets: Int,
        val totalParts: Int,
        val placedParts: Int,
        val skippedParts: List<Part>,
        val efficiency: Double, // 0..1
        val unit: LengthUnit,
    )

    // region public entry point ------------------------------------------------

    fun compute(input: Input): Result {
        val sheetLen = input.sheetLength.coerceAtLeast(0.0)
        val sheetWid = input.sheetWidth.coerceAtLeast(0.0)
        val kerf     = input.kerf.coerceAtLeast(0.0)

        val flat = mutableListOf<FlatPart>()
        val skipped = mutableListOf<Part>()
        for (p in input.parts) {
            if (p.length <= 0 || p.width <= 0 || p.quantity <= 0) continue
            val fits = (p.length <= sheetLen && p.width <= sheetWid) ||
                       (input.allowRotation && p.length <= sheetWid && p.width <= sheetLen)
            if (!fits) {
                skipped += p
                continue
            }
            repeat(p.quantity) { flat += FlatPart(p.length, p.width, p.label) }
        }
        // Sort by area DESC. Big pieces first is the classical bin-packing
        // heuristic and works well for guillotine layouts too.
        flat.sortByDescending { it.length * it.width }

        if (sheetLen <= 0.0 || sheetWid <= 0.0 || flat.isEmpty()) {
            return Result(
                sheets = emptyList(),
                totalSheets = 0,
                totalParts = input.parts.sumOf { it.quantity },
                placedParts = 0,
                skippedParts = skipped,
                efficiency = 0.0,
                unit = input.unit,
            )
        }

        val sheets = mutableListOf<MutableSheet>()
        for (part in flat) {
            val sheet = sheets.firstOrNull { tryPlace(it, part, kerf, input.allowRotation) }
            if (sheet == null) {
                val fresh = MutableSheet().also { it.free += FreeRect(0.0, 0.0, sheetLen, sheetWid) }
                sheets += fresh
                tryPlace(fresh, part, kerf, input.allowRotation)
            }
        }

        val sheetArea = sheetLen * sheetWid
        val usedArea = sheets.sumOf { s -> s.placements.sumOf { it.length * it.width } }
        val efficiency = if (sheets.isNotEmpty()) usedArea / (sheets.size * sheetArea) else 0.0

        return Result(
            sheets = sheets.mapIndexed { i, s -> Sheet(i + 1, s.placements.toList()) },
            totalSheets = sheets.size,
            totalParts = input.parts.sumOf { it.quantity },
            placedParts = sheets.sumOf { it.placements.size },
            skippedParts = skipped,
            efficiency = efficiency,
            unit = input.unit,
        )
    }

    // endregion

    // region internal mutable scratch types ------------------------------------

    private class FreeRect(var x: Double, var y: Double, var length: Double, var width: Double)

    private class MutableSheet {
        val placements: MutableList<Placement> = mutableListOf()
        val free: MutableList<FreeRect> = mutableListOf()
    }

    private class FlatPart(val length: Double, val width: Double, val label: String?)

    private data class Choice(
        val freeIndex: Int,
        val rotated: Boolean,
        val shortLeftover: Double,
        val longLeftover: Double,
    )

    /**
     * Best Short Side Fit + guillotine split. Returns true when the part was
     * placed on the given sheet; false otherwise.
     */
    private fun tryPlace(
        sheet: MutableSheet,
        part: FlatPart,
        kerf: Double,
        allowRotation: Boolean,
    ): Boolean {
        var best: Choice? = null
        sheet.free.forEachIndexed { idx, free ->
            // Orientation 1: as-is.
            if (part.length <= free.length + EPS && part.width <= free.width + EPS) {
                val sl = minOf(free.length - part.length, free.width - part.width)
                val ll = maxOf(free.length - part.length, free.width - part.width)
                if (best == null || sl < best!!.shortLeftover - EPS ||
                    (kotlin.math.abs(sl - best!!.shortLeftover) < EPS && ll < best!!.longLeftover)) {
                    best = Choice(idx, rotated = false, shortLeftover = sl, longLeftover = ll)
                }
            }
            // Orientation 2: rotated 90°.
            if (allowRotation && part.width <= free.length + EPS && part.length <= free.width + EPS) {
                val sl = minOf(free.length - part.width, free.width - part.length)
                val ll = maxOf(free.length - part.width, free.width - part.length)
                if (best == null || sl < best!!.shortLeftover - EPS ||
                    (kotlin.math.abs(sl - best!!.shortLeftover) < EPS && ll < best!!.longLeftover)) {
                    best = Choice(idx, rotated = true, shortLeftover = sl, longLeftover = ll)
                }
            }
        }

        val choice = best ?: return false
        val free = sheet.free[choice.freeIndex]
        val placedLen = if (choice.rotated) part.width else part.length
        val placedWid = if (choice.rotated) part.length else part.width

        sheet.placements += Placement(
            x = free.x,
            y = free.y,
            length = placedLen,
            width = placedWid,
            label = part.label,
            rotated = choice.rotated,
        )

        // Guillotine split: replace the host free rect with the right-side
        // and bottom-side leftovers. Kerf is taken out of both because the
        // saw blade consumes that width on every cut.
        sheet.free.removeAt(choice.freeIndex)

        val rightW = free.length - placedLen - kerf
        val bottomH = free.width - placedWid - kerf

        if (rightW > EPS) {
            sheet.free += FreeRect(
                x = free.x + placedLen + kerf,
                y = free.y,
                length = rightW,
                width = placedWid,
            )
        }
        if (bottomH > EPS) {
            sheet.free += FreeRect(
                x = free.x,
                y = free.y + placedWid + kerf,
                length = free.length,
                width = bottomH,
            )
        }
        prune(sheet)
        return true
    }

    /**
     * Drop any free rectangles fully contained by another. Keeps the candidate
     * list small without affecting solution quality.
     */
    private fun prune(sheet: MutableSheet) {
        val list = sheet.free
        var i = 0
        while (i < list.size) {
            var j = i + 1
            var removedI = false
            while (j < list.size) {
                if (contains(list[i], list[j])) {
                    list.removeAt(j)
                    continue
                } else if (contains(list[j], list[i])) {
                    list.removeAt(i)
                    removedI = true
                    break
                }
                j++
            }
            if (!removedI) i++
        }
    }

    private fun contains(outer: FreeRect, inner: FreeRect): Boolean =
        inner.x >= outer.x - EPS && inner.y >= outer.y - EPS &&
        inner.x + inner.length <= outer.x + outer.length + EPS &&
        inner.y + inner.width  <= outer.y + outer.width  + EPS

    private const val EPS = 1e-6

    // endregion
}
