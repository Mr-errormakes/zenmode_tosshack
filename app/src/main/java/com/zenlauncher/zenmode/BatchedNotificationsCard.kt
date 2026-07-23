package com.zenlauncher.zenmode

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenlauncher.zenmode.ui.theme.CabinetGrotesque
import com.zenlauncher.zenmode.ui.theme.RedditMono
import com.zenlauncher.zenmode.ui.theme.ZenTheme
import com.zenlauncher.zenmode.ui.theme.rsp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BatchedNotificationsCard(
    notifications: List<BatchedNotification>,
    onClearAll: () -> Unit,
    onDismissItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ZenTheme.colors
    var isExpanded by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.bgSecondary)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Zen Notifications",
                        fontFamily = CabinetGrotesque,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.rsp,
                        color = colors.textPrimary
                    )

                    // Count badge with Reddit Mono
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.borderFocus.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${notifications.size}",
                            fontFamily = RedditMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.rsp,
                            color = colors.textBrand
                        )
                    }
                }

                if (notifications.isNotEmpty()) {
                    Text(
                        text = if (isExpanded) "Collapse" else "View All",
                        fontFamily = CabinetGrotesque,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.rsp,
                        color = colors.textSecondary
                    )
                }
            }

            if (notifications.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No interruptions captured during Zen Mode.",
                    fontFamily = CabinetGrotesque,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.rsp,
                    color = colors.textSecondary
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                val displayList = if (isExpanded) notifications else notifications.take(2)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayList.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.bgPrimary.copy(alpha = 0.5f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontFamily = CabinetGrotesque,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.rsp,
                                        color = colors.textPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = timeFormat.format(Date(item.timestamp)),
                                        fontFamily = RedditMono,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.rsp,
                                        color = colors.textSecondary
                                    )
                                }
                                if (item.text.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.text,
                                        fontFamily = CabinetGrotesque,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.rsp,
                                        color = colors.textSecondary,
                                        maxLines = 2
                                    )
                                }
                            }

                            Text(
                                text = "✕",
                                fontFamily = CabinetGrotesque,
                                fontSize = 14.rsp,
                                color = colors.textSecondary,
                                modifier = Modifier
                                    .clickable { onDismissItem(item.id) }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Clear All",
                        fontFamily = CabinetGrotesque,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.rsp,
                        color = colors.textBrand,
                        modifier = Modifier.clickable { onClearAll() }
                    )
                }
            }
        }
    }
}

data class BatchedNotification(
    val id: String,
    val title: String,
    val text: String,
    val timestamp: Long
)
