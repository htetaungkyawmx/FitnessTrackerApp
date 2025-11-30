package org.hak.fitnesstrackerapp

import android.app.Application
import org.hak.fitnesstrackerapp.database.AppDatabase

class FitnessTrackerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    companion object {
        lateinit var instance: FitnessTrackerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
