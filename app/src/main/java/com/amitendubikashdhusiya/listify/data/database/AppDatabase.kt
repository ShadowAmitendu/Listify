package com.amitendubikashdhusiya.listify.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amitendubikashdhusiya.listify.data.dao.ShoppingItemDao
import com.amitendubikashdhusiya.listify.data.dao.ShoppingListDao
import com.amitendubikashdhusiya.listify.data.entity.SavedListItem
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem
import com.amitendubikashdhusiya.listify.data.entity.ShoppingList

@Database(
    entities = [
        ShoppingItem::class,
        ShoppingList::class,
        SavedListItem::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun shoppingListDao(): ShoppingListDao
}