package jp.benkyosuruzo

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import jp.benkyosuruzo.service.ScreenTextReaderService
import jp.benkyosuruzo.settings.SettingsActivity

/**
 * Shows the privacy notice, the current accessibility service state, and a
 * shortcut to the accessibility settings screen.
 */
class MainActivity : Activity() {

    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusView = findViewById(R.id.status_text)
        findViewById<Button>(R.id.button_open_app_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.button_open_settings).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: ActivityNotFoundException) {
                // Settings screen missing on this device; nothing else to do.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    // Re-checked on every resume so the state refreshes after returning
    // from the settings screen.
    private fun updateStatus() {
        if (isServiceEnabled()) {
            statusView.text = getString(R.string.status_enabled)
            statusView.setTextColor(STATUS_ENABLED_COLOR)
        } else {
            statusView.text = getString(R.string.status_disabled)
            statusView.setTextColor(STATUS_DISABLED_COLOR)
        }
    }

    private fun isServiceEnabled(): Boolean {
        val expected = ComponentName(this, ScreenTextReaderService::class.java)
            .flattenToString()
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { it.equals(expected, ignoreCase = true) }
    }

    private companion object {
        val STATUS_ENABLED_COLOR = 0xFF2E7D32.toInt()   // green
        val STATUS_DISABLED_COLOR = 0xFFC62828.toInt()  // red
    }
}
