package com.daymark.app.ui

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.daymark.app.data.TaskEntity
import com.daymark.app.domain.ActivityRules
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class TodayState(
    val date: LocalDate,
    val tasks: List<TaskEntity> = emptyList(),
    val activities: List<ActivityEntity> = emptyList(),
) {
    val done get() = tasks.count { it.completed }
    val trackedMinutes get() = ActivityRules.totalMinutes(activities)
}

class TodayViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as DaymarkApp).repository
    // Known limitation: the date is fixed when the screen is created (no midnight rollover yet).
    val date: LocalDate = LocalDate.now()

    val state = combine(repo.tasksFor(date), repo.activitiesOn(date)) { t, a -> TodayState(date, t, a) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayState(date))

    fun addTask(title: String, estimate: Int?) = viewModelScope.launch { repo.addTask(title, date, estimate) }
    fun toggle(task: TaskEntity) = viewModelScope.launch { repo.setTaskCompleted(task.id, !task.completed) }
    fun deleteTask(task: TaskEntity) = viewModelScope.launch { repo.deleteTask(task.id) }
    fun addActivity(desc: String, s: Long, e: Long, zone: String) = viewModelScope.launch {
        repo.saveActivity(ActivityEntity(description = desc, startMillis = s, endMillis = e, zoneId = zone))
    }
}

private enum class Capture { None, Choose, Task, Activity }

@Composable
fun TodayScreen(vm: TodayViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var capture by remember { mutableStateOf(Capture.None) }
    var pendingDelete by remember { mutableStateOf<TaskEntity?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(state.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
                    style = MaterialTheme.typography.titleMedium)
                Text(greeting(), style = MaterialTheme.typography.headlineSmall)
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Plan progress", style = MaterialTheme.typography.labelLarge)
                        val total = state.tasks.size
                        Text(if (total == 0) "No tasks planned for today" else "${state.done} of $total tasks done",
                            style = MaterialTheme.typography.titleMedium)
                        if (total > 0) {
                            LinearProgressIndicator(progress = { state.done / total.toFloat() },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        }
                        Text("Tracked today: " + ActivityRules.formatDuration(state.trackedMinutes) +
                            " (only time you've logged; unlogged time is unknown, not idle)",
                            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            if (state.tasks.isEmpty()) {
                item { Text("Tap + to add your first task or log an activity.", style = MaterialTheme.typography.bodyMedium) }
            }
            items(state.tasks, key = { it.id }) { task ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(task.completed, { vm.toggle(task) })
                        Column(Modifier.weight(1f)) {
                            Text(task.title, style = MaterialTheme.typography.bodyLarge)
                            task.estimateMinutes?.let {
                                Text("Est. ${ActivityRules.formatDuration(it.toLong())}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        IconButton({ pendingDelete = task }) { Icon(Icons.Filled.Delete, contentDescription = "Delete task") }
                    }
                }
            }
        }
        FloatingActionButton({ capture = Capture.Choose },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
            Icon(Icons.Filled.Add, contentDescription = "Quick capture")
        }
    }

    when (capture) {
        Capture.Choose -> AlertDialog(
            onDismissRequest = { capture = Capture.None },
            title = { Text("Quick capture") },
            text = { Text("Ideas and goals can't be captured yet (Milestones 2 and 6).") },
            confirmButton = { TextButton({ capture = Capture.Task }) { Text("Task") } },
            dismissButton = { TextButton({ capture = Capture.Activity }) { Text("Activity") } },
        )
        Capture.Task -> AddTaskDialog({ capture = Capture.None }) { t, e -> vm.addTask(t, e); capture = Capture.None }
        Capture.Activity -> AddActivityDialog(state.date, state.activities, { capture = Capture.None }) { d, s, e, z ->
            vm.addActivity(d, s, e, z); capture = Capture.None
        }
        Capture.None -> Unit
    }

    pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete task?") },
            text = { Text("\"${task.title}\" will be permanently deleted.") },
            confirmButton = { TextButton({ vm.deleteTask(task); pendingDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton({ pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

private fun greeting(): String = when (java.time.LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Hello"
}
