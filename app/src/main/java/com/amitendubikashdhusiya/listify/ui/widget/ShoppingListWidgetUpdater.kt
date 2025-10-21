package com.amitendubikashdhusiya.listify.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility object to trigger the ShoppingListWidget to refresh immediately.
 *
 * Can be safely called from any thread or layer (UI, ViewModel, Repository).
 */
object ShoppingListWidgetUpdater {

    /**
     * Triggers a full widget refresh by calling updateAll() on ShoppingListWidget.
     * This re-invokes provideGlance(), fetching new data and re-rendering the UI.
     *
     * Example:
     *   ShoppingListWidgetUpdater.triggerWidgetUpdate(context)
     */
    fun triggerWidgetUpdate(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                ShoppingListWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
