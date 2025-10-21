package com.amitendubikashdhusiya.listify.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onToggleBought: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (!isSelectionMode) {
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onToggleBought()
                        false // Reset to settled
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        true // Allow dismiss to trigger delete with undo
                    }

                    else -> false
                }
            } else {
                false // Disable swipe in selection mode
            }
        }
    )

    // Trigger delete when dismissed
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart && !isSelectionMode) {
            onDelete()
            dismissState.reset()
        }
    }

    // Animations
    val swipeFraction = 1f
    val iconScale by animateFloatAsState(
        targetValue = 0.8f + 0.4f * swipeFraction,
        label = "iconScale"
    )
    val iconAlpha by animateFloatAsState(targetValue = swipeFraction, label = "iconAlpha")

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            item.isBought -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 300)
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else if (item.isBought) 4.dp else 2.dp,
        animationSpec = tween(durationMillis = 300)
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = !isSelectionMode,
        enableDismissFromEndToStart = !isSelectionMode,
        modifier = modifier,
        backgroundContent = {
            if (!isSelectionMode) {
                val direction = dismissState.dismissDirection
                val bgColor = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surface
                }
                val icon = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.CheckCircle
                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                    else -> null
                }
                val iconTint = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
                val alignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
                val text = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> if (item.isBought) "Mark as To Buy" else "Mark as Bought"
                    SwipeToDismissBoxValue.EndToStart -> "Delete"
                    else -> ""
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp),
                    contentAlignment = alignment
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(iconAlpha)
                    ) {
                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .scale(iconScale)
                                )
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelLarge,
                                color = iconTint
                            )
                        } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelLarge,
                                color = iconTint
                            )
                            icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .scale(iconScale)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) {
                            onClick()
                        } else {
                            onToggleBought()
                        }
                    },
                    onLongClick = onLongClick
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .alpha(
                        animateFloatAsState(
                            targetValue = if (item.isBought && !isSelectionMode) 0.7f else 1f,
                            animationSpec = tween(durationMillis = 300)
                        ).value
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selection checkbox or bought icon
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onClick() }
                        )
                    } else if (item.isBought) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Bought",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = if (item.isBought && !isSelectionMode)
                                TextDecoration.LineThrough
                            else
                                TextDecoration.None
                        )
                        Text(
                            item.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (item.isBought && !isSelectionMode)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}