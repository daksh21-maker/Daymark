package com.daymark.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

private fun newId() = UUID.randomUUID().toString()

/** dueDate is an ISO local date (yyyy-MM-dd) so "today" is stable across time zones. */
@Entity(tableName = "tasks", indices = [Index("dueDate")])
data class TaskEntity(
    @PrimaryKey val id: String = newId(),
    val title: String,
    val description: String? = null,
    val dueDate: String? = null,
    val priority: Int = 0,
    val estimateMinutes: Int? = null,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Times are epoch millis (UTC instants); zoneId records where the user was when logging. */
@Entity(
    tableName = "activities",
    indices = [Index("startMillis"), Index("endMillis"), Index("categoryId")],
)
data class ActivityEntity(
    @PrimaryKey val id: String = newId(),
    val description: String,
    val startMillis: Long,
    val endMillis: Long,
    val zoneId: String,
    val categoryId: String? = null,   // null = Unclassified
    val notes: String? = null,
    val source: String = SOURCE_USER,
    val classificationStatus: String = CLASSIFICATION_NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val durationMillis: Long get() = endMillis - startMillis

    companion object {
        const val SOURCE_USER = "USER"
        const val CLASSIFICATION_NONE = "NONE"
    }
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    val sortOrder: Int,
    val isDefault: Boolean = true,
)
