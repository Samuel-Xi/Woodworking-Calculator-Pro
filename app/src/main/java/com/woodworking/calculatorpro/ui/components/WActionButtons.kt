package com.woodworking.calculatorpro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * Standard "Copy results" + "Save to history" action row that lives at the
 * bottom of every calculator screen. Buttons gently scale on press for tactile
 * feedback.
 */
@Composable
fun WResultActions(
    onCopy: () -> Unit,
    onSave: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    copyLabel: String = "Copy",
    saveLabel: String = "Save",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SpringButton(
            onClick = onCopy,
            enabled = enabled,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                Icons.Rounded.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = copyLabel,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        SpringPrimaryButton(
            onClick = onSave,
            enabled = enabled,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                Icons.Rounded.BookmarkAdd,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = saveLabel,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun SpringButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.65f),
        label = "btn-scale",
    )
    val borderColor by animateColorAsState(
        targetValue = if (pressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                      else MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btn-border",
    )
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.scale(scale).height(60.dp),
        shape = MaterialTheme.shapes.extraLarge,
        interactionSource = source,
        border = BorderStroke(1.6.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
        ),
        content = content,
    )
}

@Composable
private fun SpringPrimaryButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.65f),
        label = "btn-scale-primary",
    )
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.scale(scale).height(60.dp),
        shape = MaterialTheme.shapes.extraLarge,
        interactionSource = source,
        // Elevated to give the primary CTA visible 3D presence.
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        content = content,
    )
}
