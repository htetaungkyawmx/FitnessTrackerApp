package org.hak.fitnesstrackerapp

import android.app.Application
import org.hak.fitnesstrackerapp.database.AppDatabase

class FitnessTrackerApp : Application() {

    companion object {
        lateinit var instance: FitnessTrackerApp
            private set
    }

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}