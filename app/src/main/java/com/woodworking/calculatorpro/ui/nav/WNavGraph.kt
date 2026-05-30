package com.woodworking.calculatorpro.ui.nav

import android.app.Activity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.woodworking.calculatorpro.WoodworkingApp
import com.woodworking.calculatorpro.ui.screens.boardcut.BoardCutScreen
import com.woodworking.calculatorpro.ui.screens.boardfeet.BoardFeetScreen
import com.woodworking.calculatorpro.ui.screens.ceiling.CeilingScreen
import com.woodworking.calculatorpro.ui.screens.convert.UnitConvertScreen
import com.woodworking.calculatorpro.ui.screens.flooring.FlooringScreen
import com.woodworking.calculatorpro.ui.screens.history.HistoryScreen
import com.woodworking.calculatorpro.ui.screens.home.HomeScreen
import com.woodworking.calculatorpro.ui.screens.lumber.LumberRefScreen
import com.woodworking.calculatorpro.ui.screens.miter.MiterScreen
import com.woodworking.calculatorpro.ui.screens.paint.PaintScreen
import com.woodworking.calculatorpro.ui.screens.paywall.PaywallScreen
import com.woodworking.calculatorpro.ui.screens.sheetcut.SheetCutScreen
import com.woodworking.calculatorpro.ui.screens.spacing.SpacingScreen
import com.woodworking.calculatorpro.ui.screens.stair.StairScreen

/**
 * App navigation. Uses a horizontal slide for "drill in" transitions, fading
 * the home screen out — feels cohesive without being flashy.
 *
 * The graph also enforces the freemium gate: any attempt to navigate to a
 * route listed in [ProTools] is redirected to [Routes.Paywall] unless the
 * user already owns the Pro IAP. We do that at the navigation layer (not
 * inside each screen) so the same rule applies to deep links, history
 * re-opens, and future entry points.
 */
@Composable
fun WNavGraph(
    navController: NavHostController,
    activity: Activity? = null,
) {
    val enter = slideInHorizontally(animationSpec = tween(240)) { it / 6 } + fadeIn(tween(240))
    val exit = fadeOut(tween(180))
    val popEnter = fadeIn(tween(180))
    val popExit = slideOutHorizontally(animationSpec = tween(220)) { it / 6 } + fadeOut(tween(180))

    val entitlements = WoodworkingApp.get().entitlements
    val isPro by entitlements.isPro.collectAsState()

    /**
     * Centralised "navigate to tool" handler. Redirects locked routes to the
     * paywall instead of opening the tool. Free routes always go straight in.
     */
    val openTool: (String) -> Unit = { route ->
        if (ProTools.isPro(route) && !isPro) {
            navController.navigate(Routes.Paywall)
        } else {
            navController.navigate(route)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        enterTransition = { enter },
        exitTransition = { exit },
        popEnterTransition = { popEnter },
        popExitTransition = { popExit },
    ) {
        composable(Routes.Home) {
            HomeScreen(
                onOpenTool = openTool,
                onOpenHistory = { navController.navigate(Routes.History) },
                onOpenPaywall = { navController.navigate(Routes.Paywall) },
                isPro = isPro,
            )
        }
        composable(Routes.Miter)    { MiterScreen(onBack = navController::popBackStack) }
        composable(Routes.Flooring) { FlooringScreen(onBack = navController::popBackStack) }
        composable(Routes.Stair)    { StairScreen(onBack = navController::popBackStack) }
        composable(Routes.BoardCut) { BoardCutScreen(onBack = navController::popBackStack) }
        composable(Routes.SheetCut) { SheetCutScreen(onBack = navController::popBackStack) }
        composable(Routes.BoardFeet){ BoardFeetScreen(onBack = navController::popBackStack) }
        composable(Routes.Spacing)  { SpacingScreen(onBack = navController::popBackStack) }
        composable(Routes.Ceiling)  { CeilingScreen(onBack = navController::popBackStack) }
        composable(Routes.Paint)    { PaintScreen(onBack = navController::popBackStack) }
        composable(Routes.Convert)  { UnitConvertScreen(onBack = navController::popBackStack) }
        composable(Routes.Lumber)   { LumberRefScreen(onBack = navController::popBackStack) }
        composable(Routes.History)  { HistoryScreen(onBack = navController::popBackStack) }
        composable(Routes.Paywall)  {
            PaywallScreen(onBack = navController::popBackStack, activity = activity)
        }
    }
}
