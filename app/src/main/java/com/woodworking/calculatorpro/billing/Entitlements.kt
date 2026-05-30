package com.woodworking.calculatorpro.billing

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * On-device store for "is this user Pro?". Deliberately uses SharedPreferences
 * (not DataStore) so we don't pull in another dependency. The single source of
 * truth is Google Play's purchase ledger; this class only mirrors the latest
 * known state so the UI doesn't have to await a Billing round-trip on cold
 * start. [BillingManager] reconciles the mirror with Play whenever the app
 * comes to the foreground.
 *
 * Privacy posture: the only thing persisted is a boolean. No purchase tokens
 * are written here — those live with Google Play.
 */
class Entitlements(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPro = MutableStateFlow(prefs.getBoolean(KEY_PRO, false))

    /** Observe the user's current Pro state. Emits immediately. */
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    /** Snapshot the current Pro state without subscribing. */
    fun isProNow(): Boolean = _isPro.value

    /**
     * Persist a new Pro state. Idempotent — repeated identical updates are
     * no-ops so flow collectors don't get spurious emissions.
     */
    fun setPro(pro: Boolean) {
        if (_isPro.value == pro) return
        prefs.edit().putBoolean(KEY_PRO, pro).apply()
        _isPro.value = pro
    }

    companion object {
        private const val PREFS_NAME = "wcp_entitlements"
        private const val KEY_PRO = "is_pro"
    }
}
