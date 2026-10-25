package com.example.bankimukhonde.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** "Are you sure?" dialog used before anything is deleted. */
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Shows a record's details with Edit (optional) and Delete buttons. */
@Composable
fun RecordActionDialog(
    title: String,
    details: String,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(details) },
        confirmButton = {
            Row {
                if (onEdit != null) {
                    TextButton(onClick = onEdit) { Text("Edit") }
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

/**
 * A simple edit form in a dialog. One text field per label.
 * validate() returns an error message, or null when everything is fine.
 */
@Composable
fun EditDialog(
    title: String,
    labels: List<String>,
    initialValues: List<String>,
    validate: (List<String>) -> String?,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val values = remember { mutableStateListOf<String>().apply { addAll(initialValues) } }
    var errorText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                labels.forEachIndexed { index, label ->
                    AppTextField(
                        value = values[index],
                        onValueChange = { values[index] = it },
                        label = label
                    )
                }
                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val problem = validate(values.toList())
                if (problem != null) errorText = problem else onSave(values.toList())
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}