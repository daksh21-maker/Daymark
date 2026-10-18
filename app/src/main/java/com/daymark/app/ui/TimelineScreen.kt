package com.daymark.app.ui

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daymark.app.DaymarkApp
import com.daymark.app.data.ActivityEntity
import com.daymark.app.domain.ActivityRules
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TimelineViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as DaymarkApp).repository
    val date: LocalDate = LocalDate.now()
    val activities = repo.activitiesOn(date)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(desc: String, s: Long, e: Long, zone: String) = viewModelScope.launch {
        repo.saveActivity(ActivityEntity(description = desc, startMillis = s, endMillis = e, zoneId = zone))
    }
    fun delete(a: ActivityEntity) = viewModelScope.launch { repo.deleteActivity(a.id) }
    fun restore(a: ActivityEntity) = viewModelScope.launch { repo.saveActivity(a) }
}

@Composable
fun TimelineScreen(vm: TimelineViewModel = viewModel()) {
    val items by vm.activities.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton({ showAdd = true }) { Icon(Icons.Filled.Add, contentDescription = "Log activity") }
        },
    ) { inner ->
        if (items.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(inner).padding(32.dp), verticalArrangement = Arrangement.Center) {
                Text("No activities logged today", style = MaterialTheme.typography.titleMedium)
                Text("Tap + to log what you did. Gaps are shown as unknown time, never assumed.",
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items, key = { it.id }) { a ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(a.description, style = MaterialTheme.typography.bodyLarge)
                                Text("${formatClock(a.startMillis, a.zoneId)}–${formatClock(a.endMillis, a.zoneId)} · " +
                                    ActivityRules.formatDuration(a.durationMillis / 60_000),
                                    style = MaterialTheme.typography.bodySmall)
                                Text("Entered by you · Unclassified", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton({
                                vm.delete(a)
                                scope.launch {
                                    val r = snackbar.showSnackbar("Activity deleted", "Undo")
                                    if (r == SnackbarResult.ActionPerformed) vm.restore(a)
                                }
                            }) { Icon(Icons.Filled.Delete, contentDescription = "Delete activity") }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddActivityDialog(vm.date, items, { showAdd = false }) { d, s, e, z -> vm.add(d, s, e, z); showAdd = false }
    }
}
