package com.daymark.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.daymark.app.data.ActivityEntity
import com.daymark.app.domain.ActivityRules
import com.daymark.app.domain.ParseResult
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onSave: (title: String, estimateMinutes: Int?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var estimate by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New task") },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(estimate, { estimate = it.filter(Char::isDigit).take(4) },
                    label = { Text("Estimate in minutes (optional)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = { onSave(title, estimate.toIntOrNull()) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * Overlaps produce a warning and require a second tap ("Save anyway").
 * Nothing is moved or deleted to resolve a conflict.
 */
@Composable
fun AddActivityDialog(
    date: LocalDate,
    existing: List<ActivityEntity>,
    onDismiss: () -> Unit,
    onSave: (description: String, startMillis: Long, endMillis: Long, zoneId: String) -> Unit,
) {
    val zone = ZoneId.systemDefault()
    var desc by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var overlapWarning by remember { mutableStateOf<String?>(null) }

    fun edited() { error = null; overlapWarning = null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log activity") },
        text = {
            Column {
                OutlinedTextField(desc, { desc = it; edited() }, label = { Text("What did you do?") },
                    modifier = Modifier.fillMaxWidth())
                Row(Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(start, { start = it; edited() }, label = { Text("Start HH:mm") }, singleLine = true,
                        modifier = Modifier.weight(1f).padding(end = 4.dp))
                    OutlinedTextField(end, { end = it; edited() }, label = { Text("End HH:mm") }, singleLine = true,
                        modifier = Modifier.weight(1f).padding(start = 4.dp))
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                overlapWarning?.let { Text(it, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            TextButton(enabled = desc.isNotBlank(), onClick = {
                when (val r = ActivityRules.parseRange(date, zone, start, end)) {
                    is ParseResult.Error -> error = r.message
                    is ParseResult.Ok -> {
                        val clashes = ActivityRules.overlaps(r.startMillis, r.endMillis, existing)
                        if (clashes.isNotEmpty() && overlapWarning == null) {
                            overlapWarning = "Overlaps with: " + clashes.joinToString { it.description } +
                                ". Nothing will be changed. Tap again to save anyway."
                        } else {
                            onSave(desc.trim(), r.startMillis, r.endMillis, zone.id)
                        }
                    }
                }
            }) { Text(if (overlapWarning != null) "Save anyway" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
