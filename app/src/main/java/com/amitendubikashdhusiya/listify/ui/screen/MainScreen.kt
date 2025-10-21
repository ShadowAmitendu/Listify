package com.amitendubikashdhusiya.listify.ui.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amitendubikashdhusiya.listify.ListifyApplication
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem
import com.amitendubikashdhusiya.listify.ui.components.AddItemBottomSheet
import com.amitendubikashdhusiya.listify.ui.components.CategoryHeader
import com.amitendubikashdhusiya.listify.ui.components.ShoppingItemCard
import com.amitendubikashdhusiya.listify.ui.viewmodel.FilterType
import com.amitendubikashdhusiya.listify.ui.viewmodel.ShoppingViewModel
import com.amitendubikashdhusiya.listify.ui.viewmodel.ShoppingViewModelFactory
import kotlinx.coroutines.launch

data class ShareOptions(
    var showHeading: Boolean = true,
    var customHeading: String = "My Shopping List",
    var showSerialNumbers: Boolean = true,
    var showCheckboxes: Boolean = false,
    var showSummary: Boolean = true,
    var showFooter: Boolean = true,
    var groupByCategory: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val repository = (context.applicationContext as ListifyApplication).repository
    val viewModel: ShoppingViewModel = viewModel(factory = ShoppingViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSaveListDialog by remember { mutableStateOf(false) }
    var showSavedListsScreen by remember { mutableStateOf(false) }
    var saveListName by remember { mutableStateOf("") }

    // Multi-select state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Share options dialog
    var showShareOptionsDialog by remember { mutableStateOf(false) }
    var itemsToShare by remember { mutableStateOf<List<ShoppingItem>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }

    // Exit selection mode
    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems = emptySet()
    }

    // Handle back button press
    BackHandler(enabled = isSelectionMode || selectedCategory != null || isSearchActive) {
        when {
            isSelectionMode -> exitSelectionMode()
            selectedCategory != null -> selectedCategory = null
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
        }
    }

    // Saved Lists Screen
    if (showSavedListsScreen) {
        SavedListsScreen(
            lists = uiState.savedLists,
            onBack = { showSavedListsScreen = false },
            onLoadList = { list ->
                showSavedListsScreen = false
                viewModel.loadList(
                    listId = list.id,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("List \"${list.name}\" loaded successfully")
                        }
                    },
                    onError = { error ->
                        scope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                    }
                )
            },
            onUnloadList = { list ->
                viewModel.unloadList(
                    listId = list.id,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar("List \"${list.name}\" unloaded")
                        }
                    },
                    onError = { error ->
                        scope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                    }
                )
            },
            onDeleteList = { list ->
                viewModel.deleteList(list) {
                    scope.launch { snackbarHostState.showSnackbar("\"${list.name}\" deleted") }
                }
            },
            onRenameList = { list, newName ->
                viewModel.renameList(list, newName) {
                    scope.launch { snackbarHostState.showSnackbar("List renamed to \"$newName\"") }
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // Selection Mode Top Bar
                TopAppBar(
                    title = { Text("${selectedItems.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { exitSelectionMode() }) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                // Select all visible items
                                val allItemIds = uiState.items.map { it.id }.toSet()
                                selectedItems = if (selectedItems.size == allItemIds.size) {
                                    emptySet()
                                } else {
                                    allItemIds
                                }
                            }
                        ) {
                            Icon(Icons.Default.SelectAll, "Select All")
                        }

                        IconButton(
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    showSaveListDialog = true
                                }
                            },
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Save, "Save Selected")
                        }

                        IconButton(
                            onClick = {
                                if (selectedItems.isNotEmpty()) {
                                    itemsToShare = uiState.items.filter { it.id in selectedItems }
                                    showShareOptionsDialog = true
                                }
                            },
                            enabled = selectedItems.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Share, "Share Selected")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            } else if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search items...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.1f
                                ),
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            when {
                                selectedCategory != null -> selectedCategory!!
                                uiState.selectedFilter == FilterType.TO_BUY -> "To Buy"
                                uiState.selectedFilter == FilterType.BOUGHT -> "Bought"
                                else -> "Listify"
                            }
                        )
                    },
                    navigationIcon = {
                        // Show back arrow when category is selected
                        if (selectedCategory != null) {
                            IconButton(onClick = { selectedCategory = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to All")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Saved Lists") },
                                leadingIcon = { Icon(Icons.Default.Bookmark, null) },
                                onClick = {
                                    showSavedListsScreen = true
                                    showMenu = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Share All") },
                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                onClick = {
                                    itemsToShare = uiState.items
                                    showShareOptionsDialog = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export All to PDF") },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                                onClick = {
                                    scope.launch {
                                        val pdfExporter =
                                            com.amitendubikashdhusiya.listify.util.PdfExporter(
                                                context
                                            )
                                        val uri = pdfExporter.exportToPdf(uiState.items)
                                        if (uri != null) {
                                            pdfExporter.sharePdf(uri)
                                            snackbarHostState.showSnackbar("PDF exported")
                                        } else {
                                            snackbarHostState.showSnackbar("Export failed")
                                        }
                                    }
                                    showMenu = false
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips (hidden in selection mode)
            if (!isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedFilter == FilterType.ALL && selectedCategory == null,
                        onClick = {
                            viewModel.setFilter(FilterType.ALL)
                            selectedCategory = null
                        },
                        label = { Text("All") },
                        leadingIcon = if (uiState.selectedFilter == FilterType.ALL && selectedCategory == null) {
                            { Icon(Icons.AutoMirrored.Filled.List, null, Modifier.size(18.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = uiState.selectedFilter == FilterType.TO_BUY,
                        onClick = {
                            viewModel.setFilter(FilterType.TO_BUY)
                            selectedCategory = null
                        },
                        label = { Text("To Buy") },
                        leadingIcon = if (uiState.selectedFilter == FilterType.TO_BUY) {
                            { Icon(Icons.Default.ShoppingCart, null, Modifier.size(18.dp)) }
                        } else null
                    )

                    FilterChip(
                        selected = uiState.selectedFilter == FilterType.BOUGHT,
                        onClick = {
                            viewModel.setFilter(FilterType.BOUGHT)
                            selectedCategory = null
                        },
                        label = { Text("Bought") },
                        leadingIcon = if (uiState.selectedFilter == FilterType.BOUGHT) {
                            { Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp)) }
                        } else null
                    )
                }

                // Category Chips
                if (uiState.categories.isNotEmpty()) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.categories.take(3).forEach { category ->
                            SuggestionChip(
                                onClick = {
                                    selectedCategory =
                                        if (selectedCategory == category) null else category
                                    viewModel.setFilter(FilterType.ALL)
                                },
                                label = { Text(category) },
                                colors = if (selectedCategory == category) {
                                    SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                } else {
                                    SuggestionChipDefaults.suggestionChipColors()
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp)
            }

            val displayItems = if (selectedCategory != null) {
                uiState.items.filter { it.category == selectedCategory }
            } else {
                uiState.items
            }

            val filteredItems = if (searchQuery.isNotBlank()) {
                displayItems.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true)
                }
            } else {
                displayItems
            }

            // List Content
            if (filteredItems.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (searchQuery.isNotBlank()) "No items found for \"$searchQuery\"" else "No items yet. Tap + to add!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val grouped = filteredItems.groupBy { it.category }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    grouped.forEach { (category, items) ->
                        item(key = "cat_$category") {
                            CategoryHeader(
                                category = category,
                                itemCount = items.size,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(items, key = { it.id }) { item ->
                            ShoppingItemCard(
                                item = item,
                                isSelected = item.id in selectedItems,
                                isSelectionMode = isSelectionMode,
                                onToggleBought = {
                                    if (!isSelectionMode) {
                                        viewModel.toggleItemBought(item)
                                    }
                                },
                                onDelete = {
                                    if (!isSelectionMode) {
                                        viewModel.deleteItem(item)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "${item.name} deleted",
                                                actionLabel = "UNDO",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoDelete(item)
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedItems = if (item.id in selectedItems) {
                                            selectedItems - item.id
                                        } else {
                                            selectedItems + item.id
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedItems = setOf(item.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Item Bottom Sheet
    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            AddItemBottomSheet(
                onSave = { name, qty, unit, cat ->
                    viewModel.addItem(name, qty, unit, cat)
                    showAddSheet = false
                },
                existingCategories = uiState.categories
            )
        }
    }

    // Save Selected Items Dialog
    if (showSaveListDialog) {
        AlertDialog(
            onDismissRequest = { showSaveListDialog = false },
            icon = { Icon(Icons.Default.Save, null) },
            title = { Text("Save Selected Items") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Save ${selectedItems.size} selected items as a list.")
                    OutlinedTextField(
                        value = saveListName,
                        onValueChange = { saveListName = it },
                        label = { Text("List Name") },
                        singleLine = true,
                        placeholder = { Text("e.g., Weekly Groceries") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (saveListName.isNotBlank()) {
                                    val itemsToSave =
                                        uiState.items.filter { it.id in selectedItems }
                                    viewModel.saveCurrentList(
                                        name = saveListName.trim(),
                                        items = itemsToSave,
                                        onSuccess = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("List \"${saveListName.trim()}\" saved successfully")
                                            }
                                            saveListName = ""
                                            showSaveListDialog = false
                                            exitSelectionMode()
                                        },
                                        onError = { error ->
                                            scope.launch { snackbarHostState.showSnackbar(error) }
                                        }
                                    )
                                }
                            }
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (saveListName.isNotBlank()) {
                            val itemsToSave = uiState.items.filter { it.id in selectedItems }
                            viewModel.saveCurrentList(
                                name = saveListName.trim(),
                                items = itemsToSave,
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("List \"${saveListName.trim()}\" saved successfully")
                                    }
                                    saveListName = ""
                                    showSaveListDialog = false
                                    exitSelectionMode()
                                },
                                onError = { error ->
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                }
                            )
                        }
                    },
                    enabled = saveListName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    saveListName = ""
                    showSaveListDialog = false
                }) { Text("Cancel") }
            }
        )
    }

    // Share Options Dialog
    if (showShareOptionsDialog) {
        ShareOptionsDialog(
            items = itemsToShare,
            onDismiss = { showShareOptionsDialog = false },
            onShare = { options ->
                shareListAsText(context, itemsToShare, options)
                showShareOptionsDialog = false
                if (isSelectionMode) {
                    exitSelectionMode()
                }
            }
        )
    }
}

@Composable
fun ShareOptionsDialog(
    items: List<ShoppingItem>,
    onDismiss: () -> Unit,
    onShare: (ShareOptions) -> Unit
) {
    var options by remember {
        mutableStateOf(ShareOptions())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Share, null) },
        title = { Text("Share Options") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Customize your shared list:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = options.customHeading,
                    onValueChange = { options = options.copy(customHeading = it) },
                    label = { Text("List Heading") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.showHeading,
                        onCheckedChange = { options = options.copy(showHeading = it) }
                    )
                    Text("Show Heading", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.showSerialNumbers,
                        onCheckedChange = { options = options.copy(showSerialNumbers = it) }
                    )
                    Text("Show Serial Numbers", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.showCheckboxes,
                        onCheckedChange = { options = options.copy(showCheckboxes = it) }
                    )
                    Text("Show Checkboxes [ ] / [x]", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.showSummary,
                        onCheckedChange = { options = options.copy(showSummary = it) }
                    )
                    Text("Show Summary", modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.showFooter,
                        onCheckedChange = { options = options.copy(showFooter = it) }
                    )
                    Text(
                        "Show Footer (Shared via Listify)",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = options.groupByCategory,
                        onCheckedChange = { options = options.copy(groupByCategory = it) }
                    )
                    Text("Group by Category", modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onShare(options) }) {
                Text("Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun shareListAsText(
    context: android.content.Context,
    items: List<ShoppingItem>,
    options: ShareOptions
) {
    if (items.isEmpty()) return

    val shareText = buildString {
        // Heading with WhatsApp formatting
        if (options.showHeading) {
            appendLine("*${options.customHeading}*")
            appendLine("----------------------")
        }

        var globalSerialNumber = 1

        if (options.groupByCategory) {
            // Group by category
            val groupedItems = items.groupBy { it.category }

            groupedItems.forEach { (category, categoryItems) ->
                appendLine()
                appendLine("*📦 ${category.uppercase()}*")

                categoryItems.forEach { item ->
                    val serial = if (options.showSerialNumbers) {
                        "${globalSerialNumber}. "
                    } else ""

                    val checkbox = if (options.showCheckboxes) {
                        if (item.isBought) "✅ " else "⬜ "
                    } else ""

                    val statusEmoji = if (!options.showCheckboxes) {
                        if (item.isBought) "✓ " else "• "
                    } else ""

                    val strikethrough = if (item.isBought && !options.showCheckboxes) "~" else ""
                    val itemText = "${item.name} _(${item.quantity} ${item.unit})_"

                    appendLine("$serial$checkbox$statusEmoji$strikethrough$itemText$strikethrough")
                    globalSerialNumber++
                }
            }
        } else {
            // No grouping - flat list
            appendLine()
            items.forEach { item ->
                val serial = if (options.showSerialNumbers) {
                    "${globalSerialNumber}. "
                } else ""

                val checkbox = if (options.showCheckboxes) {
                    if (item.isBought) "✅ " else "⬜ "
                } else ""

                val statusEmoji = if (!options.showCheckboxes) {
                    if (item.isBought) "✓ " else "• "
                } else ""

                val strikethrough = if (item.isBought && !options.showCheckboxes) "~" else ""
                val category = if (items.any { it.category != items.first().category })
                    " [${item.category}]" else ""
                val itemText = "${item.name} _(${item.quantity} ${item.unit})_$category"

                appendLine("$serial$checkbox$statusEmoji$strikethrough$itemText$strikethrough")
                globalSerialNumber++
            }
        }

        // Summary with WhatsApp formatting
        if (options.showSummary) {
            val totalItems = items.size
            val boughtItems = items.count { it.isBought }
            val remainingItems = totalItems - boughtItems
            val completionPercent = if (totalItems > 0) (boughtItems * 100) / totalItems else 0

            appendLine()
            appendLine("----------------------")
            appendLine("*Summary*")
            appendLine("Total Items: *${totalItems}*")
            appendLine("Bought: *${boughtItems}*")
            appendLine("Remaining: *${remainingItems}*")
            if (totalItems > 0) {
                appendLine("Progress: *${completionPercent}%*")
            }
        }

        // Footer with WhatsApp formatting
        if (options.showFooter) {
            appendLine()
            appendLine("_Shared via Listify_")
        }
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, options.customHeading)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Shopping List"))
}