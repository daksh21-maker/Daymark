package com.daymark.app

import android.app.Application
import androidx.room.Room
import com.daymark.app.data.DaymarkDatabase
import com.daymark.app.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DaymarkApp : Application() {
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, DaymarkDatabase::class.java, "daymark.db").build()
        repository = Repository(db)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { repository.seedDefaultsIfEmpty() }
    }
}
