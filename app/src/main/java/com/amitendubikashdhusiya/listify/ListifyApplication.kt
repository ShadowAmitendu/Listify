package com.amitendubikashdhusiya.listify

import android.app.Application
import androidx.room.Room
import com.amitendubikashdhusiya.listify.data.database.AppDatabase
import com.amitendubikashdhusiya.listify.data.repository.ShoppingRepository
import com.amitendubikashdhusiya.listify.util.PreferencesHelper

class ListifyApplication : Application() {

    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "listify_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository by lazy {
        ShoppingRepository(
            database.shoppingItemDao(),
            database.shoppingListDao()
        )
    }

    val preferencesHelper by lazy {
        PreferencesHelper(applicationContext)
    }
}