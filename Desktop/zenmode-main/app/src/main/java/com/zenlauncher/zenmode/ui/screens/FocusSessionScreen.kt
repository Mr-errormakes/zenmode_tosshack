package com.zenlauncher.zenmode.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.zenmode.AppConstants
import com.zenlauncher.zenmode.FocusSession
import com.zenlauncher.zenmode.ui.theme.CabinetGrotesque
import com.zenlauncher.zenmode.ui.theme.RedditMono
import com.zenlauncher.zenmode.ui.theme.ZenTheme
import com.zenlauncher.zenmode.ui.theme.rdp
import com.zenlauncher.zenmode.ui.theme.rsp

// ── Public entry point ────────────────────────────────────────────────

/**
 * Sheet-style composable for Focus Session control.
 * • When [activeSession] is null → shows duration picker + Start button
 * • When [activeSession] is non-null → shows live countdown ring + End button
 */
@Composable
fun FocusSessionSheet(
    activeSession: FocusSession?,
    onStartSession: (durationMinutes: Int) -> Unit,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ZenTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.bgSecondary)
            .padding(horizontal = 24.rdp, vertical = 28.rdp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(colors.textSecondary.copy(alpha = 0.3f))
        )

        Spacer(Modifier.height(20.dp))

        if (activeSession != null) {
            ActiveSessionContent(session = activeSession, onEndSession = onEndSession)
        } else {
            IdleContent(onStartSession = onStartSession)
        }
    }
}

// ── Idle: duration picker ─────────────────────────────────────────────

@Composable
private fun IdleContent(onStartSession: (Int) -> Unit) {
    val colors = ZenTheme.colors
    var selectedMinutes by remember { mutableIntStateOf(25) }

    Text(
        text = "🧘 Start Focus Session",
        fontFamily = CabinetGrotesque,
        fontWeight = FontWeight.Bold,
        fontSize = 20.rsp,
        color = colors.textPrimary
    )

    Spacer(Modifier.height(6.dp))

    Text(
        text = "Distracting apps will be hidden until your session ends.",
        fontFamily = CabinetGrotesque,
        fontWeight = FontWeight.Normal,
        fontSize = 13.rsp,
        color = colors.textSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(24.dp))

    // Duration option chips
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppConstants.FOCUS_DURATION_OPTIONS_MIN.forEach { mins ->
            val selected = selectedMinutes == mins
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) colors.borderFocus
                        else colors.bgSecondary
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) colors.borderFocus else colors.textSecondary.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedMinutes = mins }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$mins\nmin",
                    fontFamily = RedditMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.rsp,
                    color = if (selected) colors.bgPrimary else colors.textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    // Start button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.borderFocus)
            .clickable { onStartSession(selectedMinutes) }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Start $selectedMinutes min Focus",
            fontFamily = CabinetGrotesque,
            fontWeight = FontWeight.Bold,
            fontSize = 16.rsp,
            color = colors.bgPrimary
        )
    }

    Spacer(Modifier.height(8.dp))
}

// ── Active: countdown ring ────────────────────────────────────────────

@Composable
private fun ActiveSessionContent(session: FocusSession, onEndSession: () -> Unit) {
    val colors = ZenTheme.colors
    val focusGreen = Color(0xFF4CAF50)

    val animatedProgress by animateFloatAsState(
        targetValue = 1f - session.elapsedFraction,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "focus_ring"
    )

    Text(
        text = "Focus Session Active",
        fontFamily = CabinetGrotesque,
        fontWeight = FontWeight.Bold,
        fontSize = 20.rsp,
        color = colors.textPrimary
    )

    Spacer(Modifier.height(6.dp))

    Text(
        text = "Distracting apps are hidden. Stay in the zone.",
        fontFamily = CabinetGrotesque,
        fontWeight = FontWeight.Normal,
        fontSize = 13.rsp,
        color = colors.textSecondary,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(28.dp))

    // Circular countdown ring
    Box(
        modifier = Modifier
            .size(160.rdp)
            .drawBehind {
                val stroke = 10.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(inset, inset)

                // Track
                drawArc(
                    color = colors.textSecondary.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                // Progress
                drawArc(
                    color = focusGreen,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = session.remainingFormatted,
                fontFamily = RedditMono,
                fontWeight = FontWeight.Bold,
                fontSize = 32.rsp,
                color = colors.textPrimary
            )
            Text(
                text = "remaining",
                fontFamily = CabinetGrotesque,
                fontWeight = FontWeight.Normal,
                fontSize = 11.rsp,
                color = colors.textSecondary
            )
        }
    }

    Spacer(Modifier.height(28.dp))

    // End session button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgPrimary)
            .border(1.dp, colors.textSecondary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { onEndSession() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "End Session Early",
            fontFamily = CabinetGrotesque,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.rsp,
            color = colors.textSecondary
        )
    }

    Spacer(Modifier.height(8.dp))
}
