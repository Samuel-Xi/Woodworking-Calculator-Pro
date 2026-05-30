package com.woodworking.calculatorpro.ui.nav

/**
 * Single source of truth for navigation routes. Each tool has a stable
 * "toolKey" used as both a route and the History DB foreign key, so saved
 * entries can deep-link back to the right calculator.
 */
object Routes {
    const val Home      = "home"
    const val Miter     = "miter"
    const val Flooring  = "flooring"
    const val Stair     = "stair"
    const val BoardCut  = "boardcut"
    const val SheetCut  = "sheetcut"
    const val BoardFeet = "boardfeet"
    const val Spacing   = "spacing"
    const val Ceiling   = "ceiling"
    const val Paint     = "paint"
    const val Convert   = "convert"
    const val Lumber    = "lumber"
    const val History   = "history"
    const val Paywall   = "paywall"
}

/**
 * Single source of truth for what is gated behind the one-time Pro IAP.
 *
 * Free tier (lets users evaluate the app's quality and accuracy):
 *   - Miter angles, Unit converter, Lumber reference, Paint estimator
 *
 * Pro tier (where the practical workshop time-savings live):
 *   - Board cut optimizer (1D), Sheet cut optimizer (2D), Board feet & cost,
 *     Stair layout, Equal spacing, Ceiling joists, Flooring & tile, and
 *     unlimited history.
 *
 * Keeping this list here (instead of inside each screen) means the Home grid,
 * the navigation guard, and the Paywall all agree on the same source of truth.
 */
object ProTools {
    val routes: Set<String> = setOf(
        Routes.BoardCut,
        Routes.SheetCut,
        Routes.BoardFeet,
        Routes.Stair,
        Routes.Spacing,
        Routes.Ceiling,
        Routes.Flooring,
    )

    fun isPro(route: String): Boolean = route in routes
}
