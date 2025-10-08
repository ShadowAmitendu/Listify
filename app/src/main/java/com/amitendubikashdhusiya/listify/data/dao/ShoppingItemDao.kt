package com.amitendubikashdhusiya.listify.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Query("SELECT * FROM shopping_items ORDER BY category, name")
    fun getAllItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE isBought = 0 ORDER BY category, name")
    fun getToBuyItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE isBought = 1 ORDER BY category, name")
    fun getBoughtItems(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE category = :category ORDER BY name")
    fun getItemsByCategory(category: String): Flow<List<ShoppingItem>>

    @Query("SELECT DISTINCT category FROM shopping_items ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItem)

    @Update
    suspend fun updateItem(item: ShoppingItem)

    @Delete
    suspend fun deleteItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Int)
}