package com.julianjelfs.laundromatic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.julianjelfs.laundromatic.AddItemForm

@Composable
fun AddItemDialog(
    form: AddItemForm,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onIntervalChange: (String) -> Unit,
    onLastWashedChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onDismiss()
            }
        },
        title = { Text("Add laundry item") },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    value = form.intervalInDays,
                    onValueChange = onIntervalChange,
                    label = { Text("Wash frequency (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isSubmitting,
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    value = form.daysSinceLastWash,
                    onValueChange = onLastWashedChange,
                    label = { Text("Days since last wash") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isSubmitting,
                )

                form.error?.let { error ->
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss,
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = onSubmit,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp),
                        strokeWidth = 2.dp,
                    )
                }
                Text("Save")
            }
        },
    )
}
