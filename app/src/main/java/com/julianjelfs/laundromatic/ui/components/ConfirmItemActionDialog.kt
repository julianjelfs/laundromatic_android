package com.julianjelfs.laundromatic.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.julianjelfs.laundromatic.ItemAction
import com.julianjelfs.laundromatic.LaundryItem

@Composable
fun ConfirmItemActionDialog(
    item: LaundryItem,
    action: ItemAction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(action.title) },
        text = { Text("${action.message} ${item.name}?") },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(action.confirmLabel)
            }
        },
    )
}

private val ItemAction.title: String
    get() = when (this) {
        ItemAction.Delete -> "Delete item"
        ItemAction.Pause -> "Pause item"
        ItemAction.Resume -> "Resume item"
        ItemAction.Wash -> "Mark washed"
    }

private val ItemAction.message: String
    get() = when (this) {
        ItemAction.Delete -> "Delete"
        ItemAction.Pause -> "Pause"
        ItemAction.Resume -> "Resume"
        ItemAction.Wash -> "Mark as washed"
    }

private val ItemAction.confirmLabel: String
    get() = when (this) {
        ItemAction.Delete -> "Delete"
        ItemAction.Pause -> "Pause"
        ItemAction.Resume -> "Resume"
        ItemAction.Wash -> "Wash"
    }
