package com.woodworking.calculatorpro.ui.screens.paywall

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.WoodworkingApp
import com.woodworking.calculatorpro.billing.BillingManager
import com.woodworking.calculatorpro.ui.components.WCard
import com.woodworking.calculatorpro.ui.components.WScreenScaffold

/**
 * One-time IAP upsell screen. Lists what Pro unlocks, shows the localised
 * price pulled from Google Play, and exposes "Unlock Pro" + "Restore" actions.
 *
 * The screen is robust to two failure modes:
 *  - Play Billing is unavailable (no Play services, kid account, etc.): the
 *    Unlock button is disabled and an explanatory line is shown.
 *  - Product details haven't arrived yet: the button stays enabled but shows
 *    a generic label until the price flows in.
 */
@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    activity: Activity?,
) {
    val app = WoodworkingApp.get()
    val billing = app.billingManager

    val isPro by app.entitlements.isPro.collectAsState()
    val connectionState by billing.connectionState.collectAsState()
    val product by billing.proProduct.collectAsState()
    val lastEvent by billing.lastEvent.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    val purchasedMessage = stringResource(R.string.paywall_purchased)
    val cancelledMessage = stringResource(R.string.paywall_cancelled)
    val errorMessage = stringResource(R.string.paywall_error)

    // Surface billing events as snackbars and then clear them so they don't
    // re-fire on configuration changes.
    LaunchedEffect(lastEvent) {
        when (val e = lastEvent) {
            BillingManager.BillingEvent.Purchased -> snackbar.showSnackbar(purchasedMessage)
            BillingManager.BillingEvent.Cancelled -> snackbar.showSnackbar(cancelledMessage)
            is BillingManager.BillingEvent.Error -> snackbar.showSnackbar(errorMessage)
            BillingManager.BillingEvent.NotReady -> snackbar.showSnackbar(errorMessage)
            null -> Unit
        }
        if (lastEvent != null) billing.consumeEvent()
    }

    WScreenScaffold(
        title = stringResource(R.string.paywall_title),
        onBack = onBack,
        snackbarHostState = snackbar,
    ) {
        Hero(isPro = isPro)

        WCard(title = stringResource(R.string.paywall_includes)) {
            FeatureBullet(stringResource(R.string.paywall_feature_boardcut))
            FeatureBullet(stringResource(R.string.paywall_feature_sheetcut))
            FeatureBullet(stringResource(R.string.paywall_feature_boardfeet))
            FeatureBullet(stringResource(R.string.paywall_feature_stair))
            FeatureBullet(stringResource(R.string.paywall_feature_spacing))
            FeatureBullet(stringResource(R.string.paywall_feature_ceiling))
            FeatureBullet(stringResource(R.string.paywall_feature_flooring))
            FeatureBullet(stringResource(R.string.paywall_feature_history))
            FeatureBullet(stringResource(R.string.paywall_feature_fractions))
        }

        WCard(title = stringResource(R.string.paywall_promise_title)) {
            Text(
                text = stringResource(R.string.paywall_promise_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val priceLabel = product?.oneTimePurchaseOfferDetails?.formattedPrice
        val billingReady = connectionState == BillingManager.ConnectionState.READY
        val canUnlock = billingReady && product != null && !isPro
        val buyLabel = when {
            isPro -> stringResource(R.string.paywall_already_pro)
            priceLabel != null -> stringResource(R.string.paywall_unlock_priced, priceLabel)
            else -> stringResource(R.string.paywall_unlock)
        }

        Button(
            onClick = { activity?.let { billing.launchProPurchase(it) } },
            enabled = canUnlock,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        ) {
            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null)
            Spacer(Modifier.size(10.dp))
            Text(buyLabel, style = MaterialTheme.typography.titleMedium)
        }

        TextButton(
            onClick = { billing.refreshPurchases() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.paywall_restore))
        }

        if (connectionState == BillingManager.ConnectionState.UNAVAILABLE) {
            Text(
                text = stringResource(R.string.paywall_billing_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun Hero(isPro: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.primary.copy(alpha = 0.08f),
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPro) Icons.Rounded.Check else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(
                        if (isPro) R.string.paywall_hero_unlocked
                        else R.string.paywall_hero_locked
                    ),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.paywall_hero_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp).padding(top = 2.dp),
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}
