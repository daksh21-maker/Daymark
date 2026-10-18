package com.daymark.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Milestone 1: plain (unencrypted) Room database. Encryption + Keystore arrive in Milestone 3.
 * Do not describe stored data as encrypted until that milestone is done and tested.
 */
@Database(
    entities = [TaskEntity::class, ActivityEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class DaymarkDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun activityDao(): ActivityDao
    abstract fun categoryDao(): CategoryDao
}
