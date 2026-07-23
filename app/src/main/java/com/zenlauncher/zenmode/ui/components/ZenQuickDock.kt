package com.zenlauncher.zenmode.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.zenmode.ui.theme.CabinetGrotesque
import com.zenlauncher.zenmode.ui.theme.ZenTheme

/**
 * Quick-access dock that shows the 18-glyph ZenMode icon set.
 *
 * Placed next to the Lock icon in the app grid. Tapping the dock toggle
 * expands/collapses a panel of functional shortcut icons. Each icon
 * performs its designated system action.
 *
 * Isolated in its own file to avoid merge conflicts with HomeScreen.kt.
 */

data class QuickDockItem(
    val label: String,
    val icon: @Composable (Modifier) -> Unit,
    val action: (Context) -> Unit
)

@Composable
fun ZenQuickDock(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onLockClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = ZenTheme.colors
    var expanded by remember { mutableStateOf(false) }

    val dockItems = remember {
        listOf(
            QuickDockItem("Home", { mod ->
                ZenIconHome(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Go to home / launcher
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(intent)
            },
            QuickDockItem("Search", { mod ->
                ZenIconSearch(modifier = mod, size = 22.dp)
            }) { _ ->
                onSearchClick()
            },
            QuickDockItem("Message", { mod ->
                ZenIconMessage(modifier = mod, size = 22.dp)
            }) { ctx ->
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MESSAGING)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Comment", { mod ->
                ZenIconComment(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open notes / memo app
                val intent = Intent(Intent.ACTION_CREATE_NOTE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Heart", { mod ->
                ZenIconHeart(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open health / wellbeing
                val intent = ctx.packageManager.getLaunchIntentForPackage(
                    "com.google.android.apps.wellbeing"
                ) ?: Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Add", { mod ->
                ZenIconAdd(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open camera for quick capture
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Close", { mod ->
                ZenIconClose(modifier = mod, size = 22.dp)
            }) { _ ->
                // Collapse the dock
                expanded = false
            },
            QuickDockItem("More", { mod ->
                ZenIconMore(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open app drawer / all apps
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MARKET)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Verified", { mod ->
                ZenIconVerified(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open security / verified settings
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Lock", { mod ->
                ZenIconLock(modifier = mod, size = 22.dp)
            }) { _ ->
                onLockClick()
            },
            QuickDockItem("Fire", { mod ->
                ZenIconFire(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open timer / alarm (focus timer)
                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Coin", { mod ->
                ZenIconCoin(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open calculator
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALCULATOR)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Bolt", { mod ->
                ZenIconBolt(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open battery / power settings
                val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Copy", { mod ->
                ZenIconCopy(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open clipboard manager or share
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Shared from ZenMode")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(Intent.createChooser(intent, "Share"))
            },
            QuickDockItem("Delete", { mod ->
                ZenIconDelete(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open storage / manage apps
                val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Back", { mod ->
                ZenIconArrowBack(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open recent apps (this is a hint; actual recents is a system gesture)
                Toast.makeText(ctx, "Use system gesture for recents", Toast.LENGTH_SHORT).show()
            },
            QuickDockItem("Access", { mod ->
                ZenIconAccessibility(modifier = mod, size = 22.dp)
            }) { ctx ->
                // Open accessibility settings
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                safeStartActivity(ctx, intent)
            },
            QuickDockItem("Settings", { mod ->
                ZenIconSettings(modifier = mod, size = 22.dp)
            }) { _ ->
                onSettingsClick()
            }
        )
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Toggle button: tap to expand/collapse the 18-icon panel
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.bgSecondary)
                .clickable { expanded = !expanded },
            contentAlignment = Alignment.Center
        ) {
            if (expanded) {
                ZenIconClose(size = 24.dp, tint = colors.textBrand)
            } else {
                ZenIconMore(size = 24.dp, tint = colors.textBrand)
            }
        }

        // Expanded panel with all 18 icons
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.bgSecondary.copy(alpha = 0.9f))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    dockItems.chunked(6).forEach { rowItems ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowItems.forEach { item ->
                                DockIconButton(
                                    label = item.label,
                                    icon = item.icon,
                                    onClick = { item.action(context) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DockIconButton(
    label: String,
    icon: @Composable (Modifier) -> Unit,
    onClick: () -> Unit
) {
    val colors = ZenTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(Modifier)
        }
        Text(
            text = label,
            fontFamily = CabinetGrotesque,
            fontWeight = FontWeight.Normal,
            fontSize = 9.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun safeStartActivity(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to open", Toast.LENGTH_SHORT).show()
    }
}
