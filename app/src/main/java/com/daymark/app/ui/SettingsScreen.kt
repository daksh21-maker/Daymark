package com.daymark.app.ui

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daymark.app.DaymarkApp
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as DaymarkApp).repository
    fun deleteAll() = viewModelScope.launch { repo.deleteAllData() }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val ctx: Context = LocalContext.current
    val version = remember { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?" }
    var confirm by remember { mutableStateOf(false) }

    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        Text("Follows your Android light/dark setting. An in-app override is not built yet.",
            modifier = Modifier.padding(bottom = 16.dp))

        Text("Privacy", style = MaterialTheme.typography.titleMedium)
        Text("All records stay on this device. This build requests no permissions, uses no network, and has no analytics. " +
            "The local database is NOT encrypted yet (planned for Milestone 3), and there is no app lock, export or backup yet.",
            modifier = Modifier.padding(bottom = 16.dp))

        Text("Your data", style = MaterialTheme.typography.titleMedium)
        Button({ confirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) { Text("Delete all data") }

        Text("About", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        Text("Daymark $version (Milestone 1 foundation)")
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Delete all data?") },
            text = { Text("Every task and activity on this device will be permanently deleted. This cannot be undone.") },
            confirmButton = { TextButton({ vm.deleteAll(); confirm = false }) { Text("Delete everything") } },
            dismissButton = { TextButton({ confirm = false }) { Text("Cancel") } },
        )
    }
}
