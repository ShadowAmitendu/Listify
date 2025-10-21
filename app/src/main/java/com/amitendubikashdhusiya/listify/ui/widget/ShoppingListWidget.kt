package com.amitendubikashdhusiya.listify.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.amitendubikashdhusiya.listify.ListifyApplication
import com.amitendubikashdhusiya.listify.MainActivity
import com.amitendubikashdhusiya.listify.R
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ShoppingListWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ListifyApplication).repository
        val items = withContext(Dispatchers.IO) {
            try {
                repository.getAllItems().first()
            } catch (_: Exception) {
                emptyList()
            }
        }

        provideContent {
            GlanceTheme {
                WidgetContent(context, items)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context, items: List<ShoppingItem>) {
        val totalItems = items.size
        val boughtItems = items.count { it.isBought }
        val remainingItems = totalItems - boughtItems

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Listify",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(GlanceModifier.width(8.dp))

                Image(
                    provider = ImageProvider(R.drawable.ic_refresh_24),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(28.dp)
                        .clickable(actionRunCallback<RefreshWidgetCallback>())
                )

                Spacer(GlanceModifier.width(8.dp))

                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(16.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_add_24),
                        contentDescription = "Add Item"
                    )
                }
            }

            // Stats Section
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(12.dp)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatItem(totalItems.toString(), "Total", GlanceModifier.defaultWeight())
                StatItem(boughtItems.toString(), "Bought", GlanceModifier.defaultWeight())
                StatItem(remainingItems.toString(), "Pending", GlanceModifier.defaultWeight())
            }

            Spacer(GlanceModifier.height(12.dp))

            // Scrollable Items List
            if (items.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(bottom = 4.dp) // optional padding at end
                ) {
                    items(items) { item ->
                        Box(
                            modifier = GlanceModifier
                                .padding(bottom = 8.dp) // 👈 consistent space below each item
                        ) {
                            ItemRow(item)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun StatItem(count: String, label: String, modifier: GlanceModifier = GlanceModifier) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }

    @Composable
    private fun ItemRow(item: ShoppingItem) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(8.dp)
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(
                    if (item.isBought) R.drawable.ic_check_circle_24
                    else R.drawable.ic_circle_outline_24
                ),
                contentDescription = if (item.isBought) "Bought" else "Not bought",
                modifier = GlanceModifier.size(20.dp)
            )

            Spacer(GlanceModifier.width(8.dp))

            Column(GlanceModifier.defaultWeight()) {
                Text(
                    text = item.name,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onSurface
                    ),
                    maxLines = 1
                )

                Row {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                    Text(
                        text = " • ${item.category}",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptyState() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "No items yet",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "Tap + to add items",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

class ShoppingListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShoppingListWidget()
}

class RefreshWidgetCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        ShoppingListWidget().update(context, glanceId)
    }
}