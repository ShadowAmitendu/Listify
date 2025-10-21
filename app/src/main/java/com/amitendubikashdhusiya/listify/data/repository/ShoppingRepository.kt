package com.amitendubikashdhusiya.listify.data.repository

import android.content.Context
import com.amitendubikashdhusiya.listify.data.dao.ShoppingItemDao
import com.amitendubikashdhusiya.listify.data.dao.ShoppingListDao
import com.amitendubikashdhusiya.listify.data.entity.SavedListItem
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem
import com.amitendubikashdhusiya.listify.data.entity.ShoppingList
import com.amitendubikashdhusiya.listify.ui.widget.ShoppingListWidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

class ShoppingRepository(
    private val itemDao: ShoppingItemDao,
    private val listDao: ShoppingListDao,
    private val context: Context
) {
    // Item operations
    fun getAllItems(): Flow<List<ShoppingItem>> =
        itemDao.getAllItems().catch { emit(emptyList()) }

    fun getAllCategories(): Flow<List<String>> =
        itemDao.getAllCategories().catch { emit(emptyList()) }

    suspend fun insertItem(item: ShoppingItem) {
        try {
            itemDao.insertItem(item)
            // Update widget after inserting item
            ShoppingListWidgetUpdater.triggerWidgetUpdate(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateItem(item: ShoppingItem) {
        try {
            itemDao.updateItem(item)
            // Update widget after updating item
            ShoppingListWidgetUpdater.triggerWidgetUpdate(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteItem(item: ShoppingItem) {
        try {
            itemDao.deleteItem(item)
            // Update widget after deleting item
            ShoppingListWidgetUpdater.triggerWidgetUpdate(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // List operations
    fun getAllLists(): Flow<List<ShoppingList>> =
        listDao.getAllLists().catch { emit(emptyList()) }

    fun getActiveList(): Flow<ShoppingList?> =
        listDao.getActiveList().catch { emit(null) }

    suspend fun saveCurrentList(name: String, items: List<ShoppingItem>): Long {
        return try {
            if (items.isEmpty()) return -1L

            // Create the list
            val list = ShoppingList(name = name, isActive = false)
            val listId = listDao.insertList(list)

            // Save items in the separate saved_list_items table
            items.forEach { item ->
                val savedItem = SavedListItem(
                    listId = listId.toInt(),
                    name = item.name,
                    quantity = item.quantity,
                    unit = item.unit,
                    category = item.category
                )
                listDao.insertSavedListItem(savedItem)
            }

            listId
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    suspend fun loadList(listId: Int) {
        try {
            // Get the list with its saved items
            val listWithItems = listDao.getListWithItems(listId)

            if (listWithItems != null) {
                // Insert all items from the saved list into the current shopping list
                listWithItems.items.forEach { savedItem ->
                    val newItem = ShoppingItem(
                        id = 0, // Auto-generate new ID
                        name = savedItem.name,
                        quantity = savedItem.quantity,
                        unit = savedItem.unit,
                        category = savedItem.category,
                        isBought = false
                    )
                    itemDao.insertItem(newItem)
                }

                // Mark this list as active
                listDao.deactivateAllLists()
                listDao.activateList(listId)

                // Update widget after loading list
                ShoppingListWidgetUpdater.triggerWidgetUpdate(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun unloadList(listId: Int) {
        try {
            // Get the list with its saved items
            val listWithItems = listDao.getListWithItems(listId)

            if (listWithItems != null && listWithItems.list.isActive) {
                // Get all current shopping items as a snapshot
                val currentItems = itemDao.getAllItems().first()

                // Find and remove items that match the saved list items
                listWithItems.items.forEach { savedItem ->
                    val matchingItem = currentItems.firstOrNull {
                        it.name == savedItem.name &&
                                it.quantity == savedItem.quantity &&
                                it.unit == savedItem.unit &&
                                it.category == savedItem.category
                    }

                    matchingItem?.let { itemDao.deleteItem(it) }
                }

                // Deactivate the list
                listDao.deactivateAllLists()

                // Update widget after unloading list
                ShoppingListWidgetUpdater.triggerWidgetUpdate(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteList(list: ShoppingList) {
        try {
            // Delete saved items first (CASCADE will handle this, but being explicit)
            listDao.deleteListItems(list.id)

            // Delete the list
            listDao.deleteList(list)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun renameList(list: ShoppingList, newName: String) {
        try {
            listDao.updateList(list.copy(name = newName))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getListItemCount(listId: Int): Int {
        return try {
            listDao.getListItemCount(listId)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}