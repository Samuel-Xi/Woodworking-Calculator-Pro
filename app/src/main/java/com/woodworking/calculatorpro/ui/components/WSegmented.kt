package com.woodworking.calculatorpro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * Pill-shaped segmented control. Animates the indicator's "lift" so the
 * selected option visually pops above its siblings.
 */
@Composable
fun <T> WSegmented(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> String,
) {
    val shape = MaterialTheme.shapes.large
    val itemShape = MaterialTheme.shapes.medium
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(colors.surface.copy(alpha = 0.92f))
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.85f), shape)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            val isSelected = option == selected

            val bg by animateColorAsState(
                targetValue = if (isSelected) colors.primary
                              else colors.surface.copy(alpha = 0.00f),
                animationSpec = spring(stiffness = 420f, dampingRatio = 0.72f),
                label = "seg-bg"
            )
            val fg by animateColorAsState(
                targetValue = if (isSelected) colors.onPrimary
                              else colors.onSurfaceVariant,
                animationSpec = spring(stiffness = 420f, dampingRatio = 0.72f),
                label = "seg-fg"
            )
            val itemScale by animateFloatAsState(
                targetValue = if (isSelected) 1.02f else 0.98f,
                animationSpec = spring(stiffness = 360f, dampingRatio = 0.62f),
                label = "seg-scale",
            )
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .scale(itemScale)
                    .clip(itemShape)
                    .background(bg)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = colors.onPrimary.copy(alpha = if (isSelected) 0.22f else 0f),
                        shape = itemShape,
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = { onSelected(option) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.titleSmall,
                    color = fg,
                )
            }
        }
    }
}
