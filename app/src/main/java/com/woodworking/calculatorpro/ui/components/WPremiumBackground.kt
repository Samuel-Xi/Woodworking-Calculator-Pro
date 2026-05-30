package com.woodworking.calculatorpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * App-wide neutral background. We deliberately use a single solid surface
 * tone instead of stacked radial / vertical gradients so the screens read as
 * mainstream Material 3 paid utilities (e.g. Google Calculator, Files,
 * Pillar Toolbox) rather than a stylised “AI hero” background.
 */
@Composable
fun WPremiumBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        content = content,
    )
}
