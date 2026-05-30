package com.woodworking.calculatorpro.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * One key/value row inside a result card. The value field uses [AnimatedContent]
 * so live recalculations slide in vertically — a polished, restrained motion
 * that subtly confirms the calculation updated.
 */
@Composable
fun WResultRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    helper: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = false),
            )
            Box(
                modifier = if (accent) {
                    Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(100.dp),
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                } else {
                    Modifier
                }
            ) {
                AnimatedContent(
                    targetState = value,
                    transitionSpec = {
                        (slideInVertically { it / 3 } + fadeIn()) togetherWith
                                (slideOutVertically { -it / 3 } + fadeOut())
                    },
                    label = "result-value"
                ) { v ->
                    Text(
                        text = v,
                        style = if (accent) MaterialTheme.typography.headlineSmall
                                else MaterialTheme.typography.titleMedium,
                        color = if (accent) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        if (helper != null) {
            Text(
                text = helper,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            )
        }
    }
}

@Composable
fun WResultDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
    )
}
