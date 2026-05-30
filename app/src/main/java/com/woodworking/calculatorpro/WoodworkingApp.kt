package com.woodworking.calculatorpro

import android.app.Application
import com.woodworking.calculatorpro.billing.BillingManager
import com.woodworking.calculatorpro.billing.Entitlements
import com.woodworking.calculatorpro.data.AppDatabase
import com.woodworking.calculatorpro.data.HistoryRepository

/**
 * Application entry. Holds the singletons used across the app: the Room
 * database/repository, the on-device entitlement mirror, and the lazily
 * connected Google Play Billing wrapper. Kept lightweight on purpose: no DI
 * framework, no network telemetry, no analytics. All state is recreated
 * locally from this class.
 */
class WoodworkingApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.create(applicationContext) }
    val historyRepository: HistoryRepository by lazy { HistoryRepository(database.historyDao()) }

    /** Mirror of the user's Pro purchase. Source of truth is Google Play. */
    val entitlements: Entitlements by lazy { Entitlements(applicationContext) }

    /**
     * Google Play Billing wrapper. Construction is cheap; the IPC connection
     * is opened lazily when [BillingManager.connect] is called (from the
     * Activity's onStart) and torn down in onStop.
     */
    val billingManager: BillingManager by lazy {
        BillingManager(applicationContext, entitlements)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile private var instance: WoodworkingApp? = null
        fun get(): WoodworkingApp = instance
            ?: error("WoodworkingApp not initialized. Did you forget android:name in the manifest?")
    }
}
