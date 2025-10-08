// util/PreferencesHelper.kt - Updated with delete functions
package com.amitendubikashdhusiya.listify.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("listify_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_UNITS = "custom_units"
        private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
    }

    // Units Management
    fun getCustomUnits(): List<String> {
        val unitsString = prefs.getString(KEY_CUSTOM_UNITS, "") ?: ""
        return if (unitsString.isBlank()) {
            emptyList()
        } else {
            unitsString.split(",").filter { it.isNotBlank() }
        }
    }

    fun addCustomUnit(unit: String) {
        val currentUnits = getCustomUnits().toMutableList()
        if (!currentUnits.contains(unit)) {
            currentUnits.add(unit)
            prefs.edit {
                putString(KEY_CUSTOM_UNITS, currentUnits.joinToString(","))
            }
        }
    }

    fun deleteCustomUnit(unit: String) {
        val currentUnits = getCustomUnits().toMutableList()
        currentUnits.remove(unit)
        prefs.edit {
            putString(KEY_CUSTOM_UNITS, currentUnits.joinToString(","))
        }
    }

    fun getAllUnits(): List<String> {
        val defaultUnits = listOf("pcs", "kg", "g", "L", "ml", "pack", "dozen", "box")
        return (defaultUnits + getCustomUnits()).distinct()
    }

    // Categories Management
    fun getCustomCategories(): List<String> {
        val categoriesString = prefs.getString(KEY_CUSTOM_CATEGORIES, "") ?: ""
        return if (categoriesString.isBlank()) {
            emptyList()
        } else {
            categoriesString.split(",").filter { it.isNotBlank() }
        }
    }

    fun addCustomCategory(category: String) {
        val currentCategories = getCustomCategories().toMutableList()
        if (!currentCategories.contains(category)) {
            currentCategories.add(category)
            prefs.edit {
                putString(KEY_CUSTOM_CATEGORIES, currentCategories.joinToString(","))
            }
        }
    }

    fun deleteCustomCategory(category: String) {
        val currentCategories = getCustomCategories().toMutableList()
        currentCategories.remove(category)
        prefs.edit {
            putString(KEY_CUSTOM_CATEGORIES, currentCategories.joinToString(","))
        }
    }

}