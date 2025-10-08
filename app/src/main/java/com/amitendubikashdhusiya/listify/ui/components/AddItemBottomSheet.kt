// ui/components/AddItemBottomSheet.kt - With delete options for custom units and categories
package com.amitendubikashdhusiya.listify.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amitendubikashdhusiya.listify.ListifyApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemBottomSheet(
    onSave: (String, String, String, String) -> Unit,
    existingCategories: List<String>
) {
    val context = LocalContext.current
    val prefsHelper = (context.applicationContext as ListifyApplication).preferencesHelper

    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pcs") }
    var category by remember { mutableStateOf("") }
    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var showDeleteUnitDialog by remember { mutableStateOf(false) }
    var showDeleteCategoryDialog by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf("") }
    var unitToDelete by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf("") }

    var units by remember { mutableStateOf(prefsHelper.getAllUnits()) }
    val customUnits = prefsHelper.getCustomUnits()
    val customCategories = prefsHelper.getCustomCategories()

    val defaultCategories =
        listOf("Dairy", "Produce", "Bakery", "Beverages", "Snacks", "Other")
    val categories = remember(existingCategories) {
        (existingCategories + defaultCategories + customCategories).distinct().sorted()
    }

    LaunchedEffect(categories) {
        if (category.isEmpty() && categories.isNotEmpty()) {
            category = categories.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Add Item", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = qty,
                onValueChange = { qty = it },
                label = { Text("Qty") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                )

                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    units.forEach { unitOption ->
                        DropdownMenuItem(
                            text = { Text(unitOption) },
                            onClick = {
                                unit = unitOption
                                unitExpanded = false
                            },
                            trailingIcon = if (customUnits.contains(unitOption)) {
                                {
                                    IconButton(
                                        onClick = {
                                            unitToDelete = unitOption
                                            showDeleteUnitDialog = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete unit",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            } else null
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                                Text("Add Custom Unit")
                            }
                        },
                        onClick = {
                            unitExpanded = false
                            showAddUnitDialog = true
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
            )

            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            categoryExpanded = false
                        },
                        trailingIcon = if (customCategories.contains(cat) && !existingCategories.contains(
                                cat
                            )
                        ) {
                            {
                                IconButton(
                                    onClick = {
                                        categoryToDelete = cat
                                        showDeleteCategoryDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete category",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else null
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                            Text("Add Custom Category")
                        }
                    },
                    onClick = {
                        categoryExpanded = false
                        showAddCategoryDialog = true
                    }
                )
            }
        }

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onSave(name, qty, unit, category)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Item")
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add Custom Category") },
            text = {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    placeholder = { Text("e.g., Electronics, Pet Food") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategory.isNotBlank()) {
                            val formattedCategory = newCategory.trim().replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }
                            prefsHelper.addCustomCategory(formattedCategory)
                            category = formattedCategory
                            newCategory = ""
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newCategory = ""
                        showAddCategoryDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Unit Dialog
    if (showAddUnitDialog) {
        AlertDialog(
            onDismissRequest = { showAddUnitDialog = false },
            title = { Text("Add Custom Unit") },
            text = {
                OutlinedTextField(
                    value = newUnit,
                    onValueChange = { newUnit = it },
                    label = { Text("Unit Name") },
                    singleLine = true,
                    placeholder = { Text("e.g., bags, bottles, cans") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newUnit.isNotBlank()) {
                            val trimmedUnit = newUnit.trim().lowercase()
                            prefsHelper.addCustomUnit(trimmedUnit)
                            units = prefsHelper.getAllUnits()
                            unit = trimmedUnit
                            newUnit = ""
                            showAddUnitDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newUnit = ""
                        showAddUnitDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Unit Dialog
    if (showDeleteUnitDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteUnitDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Custom Unit?") },
            text = { Text("Are you sure you want to delete \"$unitToDelete\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefsHelper.deleteCustomUnit(unitToDelete)
                        units = prefsHelper.getAllUnits()
                        if (unit == unitToDelete) {
                            unit = "pcs"
                        }
                        unitToDelete = ""
                        showDeleteUnitDialog = false
                        unitExpanded = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        unitToDelete = ""
                        showDeleteUnitDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Category Dialog
    if (showDeleteCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCategoryDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Custom Category?") },
            text = { Text("Are you sure you want to delete \"$categoryToDelete\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        prefsHelper.deleteCustomCategory(categoryToDelete)
                        if (category == categoryToDelete) {
                            category = categories.firstOrNull() ?: "Other"
                        }
                        categoryToDelete = ""
                        showDeleteCategoryDialog = false
                        categoryExpanded = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        categoryToDelete = ""
                        showDeleteCategoryDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}