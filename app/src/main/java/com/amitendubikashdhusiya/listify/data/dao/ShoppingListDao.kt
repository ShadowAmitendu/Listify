package com.amitendubikashdhusiya.listify.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.amitendubikashdhusiya.listify.data.entity.SavedListItem
import com.amitendubikashdhusiya.listify.data.entity.ShoppingList
import kotlinx.coroutines.flow.Flow

data class ListWithSavedItems(
    @Embedded val list: ShoppingList,
    @Relation(
        parentColumn = "id",
        entityColumn = "listId"
    )
    val items: List<SavedListItem>
)

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllLists(): Flow<List<ShoppingList>>

    @Query("SELECT * FROM shopping_lists WHERE isActive = 1 LIMIT 1")
    fun getActiveList(): Flow<ShoppingList?>

    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getListById(listId: Int): ShoppingList?

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getListWithItems(listId: Int): ListWithSavedItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ShoppingList): Long

    @Update
    suspend fun updateList(list: ShoppingList)

    @Delete
    suspend fun deleteList(list: ShoppingList)

    @Query("UPDATE shopping_lists SET isActive = 0")
    suspend fun deactivateAllLists()

    @Query("UPDATE shopping_lists SET isActive = 1 WHERE id = :listId")
    suspend fun activateList(listId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedListItem(item: SavedListItem)

    @Query("DELETE FROM saved_list_items WHERE listId = :listId")
    suspend fun deleteListItems(listId: Int)

    @Query("SELECT COUNT(*) FROM saved_list_items WHERE listId = :listId")
    suspend fun getListItemCount(listId: Int): Int
}