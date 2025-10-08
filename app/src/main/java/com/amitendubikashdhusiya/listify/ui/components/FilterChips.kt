package com.amitendubikashdhusiya.listify.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amitendubikashdhusiya.listify.ui.viewmodel.FilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipGroup(
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == FilterType.ALL,
            onClick = { onFilterSelected(FilterType.ALL) },
            label = { Text("All") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedFilter == FilterType.TO_BUY,
            onClick = { onFilterSelected(FilterType.TO_BUY) },
            label = { Text("To Buy") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedFilter == FilterType.BOUGHT,
            onClick = { onFilterSelected(FilterType.BOUGHT) },
            label = { Text("Bought") },
            modifier = Modifier.weight(1f)
        )
    }
}