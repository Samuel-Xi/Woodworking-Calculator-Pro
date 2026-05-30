package com.woodworking.calculatorpro.domain

/**
 * North American softwood (S4S — surfaced four sides) nominal-vs-actual sizes
 * and common engineered sheet goods. Static data only — no I/O, no network.
 */
object LumberData {

    data class LumberSize(
        val nominal: String,
        val actualInches: String,
        val actualMm: String,
        val notes: String = "",
    )

    val softwood: List<LumberSize> = listOf(
        LumberSize("1×2", "3/4 × 1-1/2", "19 × 38"),
        LumberSize("1×3", "3/4 × 2-1/2", "19 × 64"),
        LumberSize("1×4", "3/4 × 3-1/2", "19 × 89"),
        LumberSize("1×6", "3/4 × 5-1/2", "19 × 140"),
        LumberSize("1×8", "3/4 × 7-1/4", "19 × 184"),
        LumberSize("1×10", "3/4 × 9-1/4", "19 × 235"),
        LumberSize("1×12", "3/4 × 11-1/4", "19 × 286"),

        LumberSize("2×2", "1-1/2 × 1-1/2", "38 × 38"),
        LumberSize("2×3", "1-1/2 × 2-1/2", "38 × 64"),
        LumberSize("2×4", "1-1/2 × 3-1/2", "38 × 89"),
        LumberSize("2×6", "1-1/2 × 5-1/2", "38 × 140"),
        LumberSize("2×8", "1-1/2 × 7-1/4", "38 × 184"),
        LumberSize("2×10", "1-1/2 × 9-1/4", "38 × 235"),
        LumberSize("2×12", "1-1/2 × 11-1/4", "38 × 286"),

        LumberSize("4×4", "3-1/2 × 3-1/2", "89 × 89"),
        LumberSize("4×6", "3-1/2 × 5-1/2", "89 × 140"),
        LumberSize("6×6", "5-1/2 × 5-1/2", "140 × 140"),
    )

    val sheetGoods: List<LumberSize> = listOf(
        LumberSize("Plywood 1/4", "0.218 (≈7/32)", "≈5.5"),
        LumberSize("Plywood 3/8", "0.344 (≈11/32)", "≈9"),
        LumberSize("Plywood 1/2", "0.469 (≈15/32)", "≈12"),
        LumberSize("Plywood 5/8", "0.594 (≈19/32)", "≈15.1"),
        LumberSize("Plywood 3/4", "0.703 (≈23/32)", "≈18"),
        LumberSize("MDF 1/2", "1/2", "12.7"),
        LumberSize("MDF 3/4", "3/4", "19"),
        LumberSize("OSB 7/16", "7/16", "11.1", "Sheathing"),
        LumberSize("OSB 23/32", "23/32", "18.3", "Subfloor"),
        LumberSize("Drywall 1/2", "1/2", "12.7"),
        LumberSize("Drywall 5/8", "5/8", "15.9", "Type X (fire)"),
    )
}
