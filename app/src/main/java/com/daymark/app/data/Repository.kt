package com.daymark.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class Repository(private val db: DaymarkDatabase) {
    private val tasks = db.taskDao()
    private val activities = db.activityDao()
    private val categories = db.categoryDao()

    fun tasksFor(date: LocalDate): Flow<List<TaskEntity>> = tasks.observeForDate(date.toString())

    fun activitiesOn(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Flow<List<ActivityEntity>> {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return activities.observeBetween(start, end)
    }

    suspend fun addTask(title: String, date: LocalDate, estimateMinutes: Int?) =
        tasks.upsert(TaskEntity(title = title.trim(), dueDate = date.toString(), estimateMinutes = estimateMinutes))

    suspend fun setTaskCompleted(id: String, done: Boolean) {
        val now = System.currentTimeMillis()
        tasks.setCompleted(id, done, if (done) now else null, now)
    }

    suspend fun deleteTask(id: String) = tasks.delete(id)

    suspend fun saveActivity(a: ActivityEntity) = activities.upsert(a.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteActivity(id: String) = activities.delete(id)

    /** Seeds editable default categories once. Category management UI arrives in Milestone 2/5. */
    suspend fun seedDefaultsIfEmpty() = withContext(Dispatchers.IO) {
        if (categories.count() == 0) {
            val names = listOf(
                "Study and Learning", "Business and Productive Work", "Personal Responsibilities",
                "Health and Exercise", "Entertainment and Casual Browsing", "Communication and Social",
                "Rest, Travel, and Miscellaneous", "Unknown or Unclassified",
            )
            categories.insertAll(names.mapIndexed { i, n -> CategoryEntity(name = n, sortOrder = i) })
        }
    }

    /** Real, irreversible deletion of all local records. */
    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        seedDefaultsIfEmpty()
    }
}
