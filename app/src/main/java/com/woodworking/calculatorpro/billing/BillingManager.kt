package com.woodworking.calculatorpro.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.woodworking.calculatorpro.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wraps Google Play Billing for a single non-consumable IAP (the Pro unlock).
 *
 * Contract:
 * - The product id [PRO_PRODUCT_ID] is an INAPP, non-consumable. Once bought
 *   it stays in the user's Google account; reinstalling the app and tapping
 *   "Restore" will re-acknowledge it for free.
 * - This class never persists purchase tokens. The only on-device state is a
 *   single boolean in [Entitlements], rebuilt from Play on every cold start.
 * - All Billing traffic happens on demand. The connection is opened lazily
 *   when the user first opens the Paywall or returns to foreground while Pro
 *   is still unknown, and is released in [release].
 *
 * Threading: the BillingClient invokes its callbacks on the main thread; we
 * stay on that thread and only touch the [Entitlements] StateFlow which is
 * thread-safe.
 */
class BillingManager(
    context: Context,
    private val entitlements: Entitlements,
) : PurchasesUpdatedListener, BillingClientStateListener {

    enum class ConnectionState { IDLE, CONNECTING, READY, DISCONNECTED, UNAVAILABLE }

    private val appContext = context.applicationContext

    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** Cached product details so the Paywall can render the localised price. */
    private val _proProduct = MutableStateFlow<ProductDetails?>(null)
    val proProduct: StateFlow<ProductDetails?> = _proProduct.asStateFlow()

    /** Last user-visible status. Pushed to a snackbar / inline message. */
    private val _lastEvent = MutableStateFlow<BillingEvent?>(null)
    val lastEvent: StateFlow<BillingEvent?> = _lastEvent.asStateFlow()

    private val client: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            // Required call in Billing 7.x; we only handle one-time products.
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    // region lifecycle ---------------------------------------------------------

    /** Open the IPC connection to Play. Safe to call repeatedly. */
    fun connect() {
        // Debug shortcut: pretend we're already Pro to make QA painless.
        if (BuildConfig.DEBUG && DEBUG_FORCE_PRO) {
            entitlements.setPro(true)
        }
        if (_connectionState.value == ConnectionState.READY ||
            _connectionState.value == ConnectionState.CONNECTING
        ) return
        _connectionState.value = ConnectionState.CONNECTING
        client.startConnection(this)
    }

    fun release() {
        if (client.isReady) client.endConnection()
        _connectionState.value = ConnectionState.IDLE
    }

    /** Clear the last event after the UI has shown it. */
    fun consumeEvent() {
        _lastEvent.value = null
    }

    // endregion

    // region BillingClientStateListener ----------------------------------------

    override fun onBillingServiceDisconnected() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                _connectionState.value = ConnectionState.READY
                queryProProductDetails()
                refreshPurchases()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                // Old Play Store, no Google account, etc. The user can still
                // use the free tier; just disable the "Unlock" button.
                _connectionState.value = ConnectionState.UNAVAILABLE
            }
            else -> {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    // endregion

    // region Pro product + purchase flow ---------------------------------------

    private fun queryProProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRO_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        client.queryProductDetailsAsync(params) { result, products ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _proProduct.value = products.firstOrNull { it.productId == PRO_PRODUCT_ID }
            }
        }
    }

    /**
     * Launch the Play Billing UI for the Pro unlock. The host must pass a
     * live Activity (the BillingClient must run on a real Window).
     */
    fun launchProPurchase(activity: Activity) {
        val product = _proProduct.value ?: run {
            _lastEvent.value = BillingEvent.NotReady
            return
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .build()
                )
            )
            .build()
        val launchResult = client.launchBillingFlow(activity, flowParams)
        if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
            _lastEvent.value = BillingEvent.Error(launchResult.debugMessage)
        }
    }

    /** Re-sync the local Pro state with Play. Idempotent and free. */
    fun refreshPurchases() {
        if (!client.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryPurchasesAsync
            val proPurchase = purchases.firstOrNull { p -> PRO_PRODUCT_ID in p.products }
            handlePurchase(proPurchase)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val pro = purchases?.firstOrNull { PRO_PRODUCT_ID in it.products }
                handlePurchase(pro)
                if (pro != null && pro.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    _lastEvent.value = BillingEvent.Purchased
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _lastEvent.value = BillingEvent.Cancelled
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // The user already has Pro on this Google account; just sync.
                refreshPurchases()
            }
            else -> {
                _lastEvent.value = BillingEvent.Error(result.debugMessage)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase?) {
        if (purchase == null) {
            // No record on Play — make sure we're not lying to the user.
            entitlements.setPro(BuildConfig.DEBUG && DEBUG_FORCE_PRO)
            return
        }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        // Grant entitlement first so the UI reflects reality immediately even
        // if the acknowledge round-trip is slow.
        entitlements.setPro(true)

        // Acknowledge non-consumable IAPs once; Play refunds unacknowledged
        // purchases after 3 days.
        if (!purchase.isAcknowledged) {
            val ack = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            client.acknowledgePurchase(ack) { /* fire-and-forget */ }
        }
    }

    // endregion

    /** UI-visible billing events. Surfaced via a snackbar in the Paywall. */
    sealed interface BillingEvent {
        data object Purchased : BillingEvent
        data object Cancelled : BillingEvent
        data object NotReady : BillingEvent
        data class Error(val message: String?) : BillingEvent
    }

    companion object {
        /**
         * Google Play Console product id for the one-time Pro unlock. Must be
         * created in the Play Console under the same applicationId with this
         * exact identifier and type "Managed product / non-consumable".
         */
        const val PRO_PRODUCT_ID = "wcp_pro_unlock"

        /**
         * When DEBUG_FORCE_PRO=true and DEBUG=true, the app behaves as if the
         * user has bought Pro. Used for screenshot generation and QA. Never
         * shipped because it's gated on BuildConfig.DEBUG.
         */
        private const val DEBUG_FORCE_PRO = false
    }
}
