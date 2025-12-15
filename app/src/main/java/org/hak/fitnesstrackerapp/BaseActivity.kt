package org.hak.fitnesstrackerapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.hak.fitnesstrackerapp.utils.PreferencesManager

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runOnUiThread {
                Toast.makeText(
                    this,
                    "App crashed: ${throwable.message}",
                    Toast.LENGTH_LONG
                ).show()
                throwable.printStackTrace()
            }
        }

        preferencesManager = PreferencesManager(this)
        setupActivity()
    }

    abstract fun setupActivity()

    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showLongToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}