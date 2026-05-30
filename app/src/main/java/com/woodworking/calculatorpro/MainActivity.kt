package com.woodworking.calculatorpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.woodworking.calculatorpro.ui.nav.WNavGraph
import com.woodworking.calculatorpro.ui.theme.WoodworkingTheme

/**
 * Single-activity host. Sets up edge-to-edge content, applies the app theme
 * (which automatically follows the system light/dark mode), and starts the
 * Compose navigation graph.
 *
 * The Google Play Billing connection is opened in onStart and released in
 * onStop. This keeps the app fully usable offline — the IPC channel only
 * exists while the activity is visible.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WoodworkingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    WNavGraph(navController = navController, activity = this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Lazy: BillingManager only opens the Play IPC channel here. If the
        // device has no Play services, it stays in UNAVAILABLE and the app
        // simply behaves as the free tier with a disabled "Unlock" button.
        WoodworkingApp.get().billingManager.connect()
    }

    override fun onResume() {
        super.onResume()
        // Re-sync purchase ledger when the user comes back from the Play
        // billing flow or installs the app on a new device.
        WoodworkingApp.get().billingManager.refreshPurchases()
    }

    override fun onStop() {
        super.onStop()
        WoodworkingApp.get().billingManager.release()
    }
}
