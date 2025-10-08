package com.amitendubikashdhusiya.listify.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for storing items that belong to saved lists.
 * This table is separate from shopping_items to ensure saved lists
 * are independent and won't be affected by deletions in the main shopping list.
 */
@Entity(
    tableName = "saved_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])]
)
data class SavedListItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val listId: Int,
    val name: String,
    val quantity: String,
    val unit: String,
    val category: String
)