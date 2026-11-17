package com.smartnotesai.feature.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.smartnotesai.feature.home.NoteSortOrder

@Composable
fun NoteSortSelector(
    selectedOrder: NoteSortOrder,
    onOrderSelected: (NoteSortOrder) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        TextButton(onClick = { expanded = true }) {
            Text("Sort: ${selectedOrder.displayName()}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NoteSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(order.displayName()) },
                    onClick = {
                        onOrderSelected(order)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun NoteSortOrder.displayName(): String {
    return when (this) {
        NoteSortOrder.RECENTLY_UPDATED -> "Recently Updated"
        NoteSortOrder.OLDEST_UPDATED -> "Oldest Updated"
        NoteSortOrder.ALPHABETICAL -> "Alphabetical (A-Z)"
    }
}