package com.amitendubikashdhusiya.listify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem
import com.amitendubikashdhusiya.listify.data.entity.ShoppingList
import com.amitendubikashdhusiya.listify.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterType {
    ALL, TO_BUY, BOUGHT
}

data class ShoppingUiState(
    val items: List<ShoppingItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedFilter: FilterType = FilterType.ALL,
    val selectedCategory: String? = null,
    val savedLists: List<ShoppingList> = emptyList(),
    val activeList: ShoppingList? = null
)

class ShoppingViewModel(
    private val repository: ShoppingRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(FilterType.ALL)
    private val _selectedCategory = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ShoppingUiState> = combine(
        repository.getAllItems(),
        repository.getAllCategories(),
        _selectedFilter,
        _selectedCategory,
        repository.getAllLists(),
        repository.getActiveList()
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val allItems = flows[0] as List<ShoppingItem>

        @Suppress("UNCHECKED_CAST")
        val categories = flows[1] as List<String>
        val filter = flows[2] as FilterType
        val selectedCat = flows[3] as String?

        @Suppress("UNCHECKED_CAST")
        val lists = flows[4] as List<ShoppingList>
        val activeList = flows[5] as ShoppingList?

        val filteredItems = when (filter) {
            FilterType.ALL -> allItems
            FilterType.TO_BUY -> allItems.filter { !it.isBought }
            FilterType.BOUGHT -> allItems.filter { it.isBought }
        }

        ShoppingUiState(
            items = filteredItems,
            categories = categories,
            selectedFilter = filter,
            selectedCategory = selectedCat,
            savedLists = lists,
            activeList = activeList
        )
    }.catch {
        emit(ShoppingUiState())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShoppingUiState()
    )

    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addItem(name: String, quantity: String, unit: String, category: String) {
        viewModelScope.launch {
            try {
                val item = ShoppingItem(
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit,
                    category = category.trim()
                )
                repository.insertItem(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleItemBought(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                repository.updateItem(item.copy(isBought = !item.isBought))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                repository.deleteItem(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun undoDelete(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                repository.insertItem(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveCurrentList(
        name: String,
        items: List<ShoppingItem>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (items.isEmpty()) {
                    onError("No items to save")
                    return@launch
                }

                val result = repository.saveCurrentList(name, items)
                if (result > 0) {
                    onSuccess()
                } else {
                    onError("Failed to save list")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun loadList(listId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.loadList(listId)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun unloadList(listId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.unloadList(listId)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun deleteList(list: ShoppingList, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteList(list)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun renameList(list: ShoppingList, newName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.renameList(list, newName)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class ShoppingViewModelFactory(
    private val repository: ShoppingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingViewModel::class.java)) {
            return ShoppingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}