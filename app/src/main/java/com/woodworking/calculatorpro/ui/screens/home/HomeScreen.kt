package com.woodworking.calculatorpro.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Architecture
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SquareFoot
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.woodworking.calculatorpro.R
import com.woodworking.calculatorpro.ui.components.WPremiumBackground
import com.woodworking.calculatorpro.ui.components.WToolTile
import com.woodworking.calculatorpro.ui.nav.ProTools
import com.woodworking.calculatorpro.ui.nav.Routes

private data class Tool(
    val route: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

/**
 * Home grid. Lists every calculator and the lumber/history pages. Two-column
 * layout on phones; the LazyVerticalGrid expands gracefully on tablets.
 *
 * Free vs Pro tools are visually marked with a small lock badge. The actual
 * gating happens at the navigation layer ([WNavGraph]) so this screen only
 * needs to *show* the state — never enforce it.
 */
@Composable
fun HomeScreen(
    onOpenTool: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenPaywall: () -> Unit,
    isPro: Boolean,
) {
    val tools = listOf(
        Tool(Routes.Miter,    stringResource(R.string.tool_miter),    stringResource(R.string.tool_miter_desc),    Icons.Rounded.Architecture),
        Tool(Routes.Flooring, stringResource(R.string.tool_flooring), stringResource(R.string.tool_flooring_desc), Icons.Rounded.GridOn),
        Tool(Routes.Stair,    stringResource(R.string.tool_stair),    stringResource(R.string.tool_stair_desc),    Icons.Rounded.Stairs),
        Tool(Routes.BoardCut, stringResource(R.string.tool_boardcut), stringResource(R.string.tool_boardcut_desc), Icons.Rounded.ContentCut),
        Tool(Routes.SheetCut, stringResource(R.string.tool_sheetcut),stringResource(R.string.tool_sheetcut_desc),Icons.Rounded.Dashboard),
        Tool(Routes.BoardFeet,stringResource(R.string.tool_boardfeet),stringResource(R.string.tool_boardfeet_desc),Icons.Rounded.TableChart),
        Tool(Routes.Spacing,  stringResource(R.string.tool_spacing),  stringResource(R.string.tool_spacing_desc),  Icons.Rounded.ViewWeek),
        Tool(Routes.Ceiling,  stringResource(R.string.tool_ceiling),  stringResource(R.string.tool_ceiling_desc),  Icons.Rounded.ViewWeek),
        Tool(Routes.Paint,    stringResource(R.string.tool_paint),    stringResource(R.string.tool_paint_desc),    Icons.Rounded.FormatPaint),
        Tool(Routes.Convert,  stringResource(R.string.tool_convert),  stringResource(R.string.tool_convert_desc),  Icons.Rounded.SwapHoriz),
        Tool(Routes.Lumber,   stringResource(R.string.tool_lumber),   stringResource(R.string.tool_lumber_desc),   Icons.Rounded.TableChart),
    )

    WPremiumBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
        ) { padding ->
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                columns = GridCells.Adaptive(minSize = 168.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    HomeHeader(onOpenHistory = onOpenHistory)
                }
                if (!isPro) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        UpgradeBanner(onOpenPaywall = onOpenPaywall)
                    }
                }
                items(items = tools, key = { it.route }) { tool ->
                    val locked = ProTools.isPro(tool.route) && !isPro
                    Box {
                        WToolTile(
                            title = tool.title,
                            description = tool.description,
                            icon = tool.icon,
                            onClick = { onOpenTool(tool.route) },
                        )
                        if (locked) {
                            // Small "Pro" pill in the top-right of the tile.
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = stringResource(R.string.home_pro_badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onOpenHistory: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.surface,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
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
                    .background(colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.SquareFoot,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onBackground,
                )
                Text(
                    stringResource(R.string.app_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(colors.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_privacy_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary,
                    )
                }
            }
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary.copy(alpha = 0.10f)),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = stringResource(R.string.tool_history),
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/**
 * Discrete "Unlock Pro" banner. Only shown when the user is not yet Pro.
 * Tapping anywhere on the card opens the Paywall.
 */
@Composable
private fun UpgradeBanner(onOpenPaywall: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onOpenPaywall),
        shape = MaterialTheme.shapes.extraLarge,
        color = colors.primary.copy(alpha = 0.08f),
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.WorkspacePremium,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.home_upgrade_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.home_upgrade_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}
