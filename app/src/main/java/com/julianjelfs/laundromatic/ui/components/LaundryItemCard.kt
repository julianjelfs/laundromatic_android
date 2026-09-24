package com.julianjelfs.laundromatic.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julianjelfs.laundromatic.ItemAction
import com.julianjelfs.laundromatic.LaundryItem
import com.julianjelfs.laundromatic.LaundryStatus
import com.julianjelfs.laundromatic.ui.theme.DarkBlue
import com.julianjelfs.laundromatic.ui.theme.OffWhite
import com.julianjelfs.laundromatic.ui.theme.SoftGreen
import com.julianjelfs.laundromatic.ui.theme.SoftRed
import com.julianjelfs.laundromatic.ui.theme.WarmYellow

@Composable
fun LaundryItemCard(
    item: LaundryItem,
    isBusy: Boolean,
    onActionClick: (LaundryItem, ItemAction) -> Unit,
) {
    val containerColor = when (item.status) {
        LaundryStatus.UnderControl -> SoftGreen
        LaundryStatus.Due -> WarmYellow
        LaundryStatus.Overdue -> SoftRed
    }
    val contentColor = if (item.status == LaundryStatus.Due) DarkBlue else OffWhite

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${item.intervalLabel} • ${item.dueLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    if (item.isPaused) {
                        Surface(
                            color = contentColor.copy(alpha = 0.14f),
                            contentColor = contentColor,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                text = "Paused",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionIconButton(
                    enabled = !isBusy,
                    onClick = { onActionClick(item, ItemAction.Delete) },
                    size = 34.dp,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete ${item.name}",
                        modifier = Modifier.size(17.dp),
                    )
                }
                ActionIconButton(
                    enabled = !isBusy,
                    onClick = {
                        onActionClick(
                            item,
                            if (item.isPaused) ItemAction.Resume else ItemAction.Pause,
                        )
                    },
                    size = 34.dp,
                ) {
                    Icon(
                        imageVector = if (item.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (item.isPaused) {
                            "Resume ${item.name}"
                        } else {
                            "Pause ${item.name}"
                        },
                        modifier = Modifier.size(17.dp),
                    )
                }
                ActionIconButton(
                    enabled = !isBusy,
                    onClick = { onActionClick(item, ItemAction.Wash) },
                    size = 34.dp,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalLaundryService,
                        contentDescription = "Wash ${item.name}",
                        modifier = Modifier.size(17.dp),
                    )
                }

                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(18.dp),
                        color = contentColor,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    size: Dp,
    icon: @Composable () -> Unit,
) {
    IconButton(
        modifier = Modifier.size(size),
        enabled = enabled,
        onClick = onClick,
    ) {
        icon()
    }
}
