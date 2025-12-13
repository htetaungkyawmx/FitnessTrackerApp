package org.hak.fitnesstrackerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.hak.fitnesstrackerapp.utils.PreferencesManager

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(this)
        setupActivity()
    }

    abstract fun setupActivity()

    protected fun checkLogin(): Boolean {
        return preferencesManager.isLoggedIn && preferencesManager.userId != -1
    }

    protected fun getUserId(): Int {
        return preferencesManager.userId
    }
}